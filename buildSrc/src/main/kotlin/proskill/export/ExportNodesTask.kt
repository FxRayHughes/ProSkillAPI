package proskill.export

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.lang.reflect.Modifier
import java.net.URL
import java.net.URLClassLoader

/**
 * 从插件源码的 @SkillNode / @SkillField 注解导出编辑器所需的节点元数据。
 *
 * 此前编辑器的节点数据是用脚本从旧编辑器的 JS 反向抽取的，插件新增节点编辑器无从得知。
 * 本任务把数据源倒转过来：以编译产物为准，注解即契约。
 *
 * 输出格式沿用 new-editor 既有的 legacy.generated.json 契约
 * （id/name/category/container/description/fields[] 及各 *Zh 字段），
 * 因此编辑器无需改动读取逻辑。
 */
abstract class ExportNodesTask : DefaultTask() {

    /** 主工程与依赖的 class 路径，用于反射加载节点类。 */
    @get:InputFiles
    abstract val classpath: ConfigurableFileCollection

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @TaskAction
    fun export() {
        val urls = classpath.files.map { it.toURI().toURL() }.toTypedArray<URL>()
        val loader = URLClassLoader(urls, javaClass.classLoader)

        val nodeAnnotation = loadClass(loader, "com.sucy.skill.dynamic.meta.SkillNode")
        val fieldAnnotation = loadClass(loader, "com.sucy.skill.dynamic.meta.SkillField")
        if (nodeAnnotation == null || fieldAnnotation == null) {
            logger.warn("找不到注解类型，跳过节点导出")
            return
        }

        val classesDir = classpath.files.firstOrNull { it.isDirectory && File(it, "com/sucy/skill/dynamic").isDirectory }
        if (classesDir == null) {
            logger.warn("找不到 dynamic 包的编译产物，跳过节点导出")
            return
        }

        val gson = GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()
        val nodes = JsonArray()
        var fieldCount = 0
        val missingKey = mutableListOf<String>()
        val unregistered = mutableListOf<String>()

        // ComponentRegistry 是节点可用性的唯一权威：没注册的类配置里写了也创建不出来。
        val registeredKeys = readRegisteredKeys(classesDir)

        // 逐类别扫描，类别即节点分类，无需在注解里重复声明
        for (category in listOf("trigger", "target", "condition", "mechanic")) {
            val dir = File(classesDir, "com/sucy/skill/dynamic/$category")
            if (!dir.isDirectory) continue

            val classNames = dir.listFiles { f -> f.name.endsWith(".class") && !f.name.contains('$') }
                ?.map { "com.sucy.skill.dynamic.$category.${it.name.removeSuffix(".class")}" }
                ?.sorted()
                ?: emptyList()

            for (className in classNames) {
                val type = loadClass(loader, className) ?: continue
                @Suppress("UNCHECKED_CAST")
                val node = type.getAnnotation(nodeAnnotation as Class<Annotation>) ?: continue

                val key = annotationString(node, "key")
                // 注解里的 key 必须与 getKey() 一致，否则编辑器写出的配置插件读不到
                val actualKey = readGetKey(category, type.simpleName)
                if (actualKey != null && actualKey != key) {
                    missingKey.add("$className: 注解 key=\"$key\" 与 getKey()=\"$actualKey\" 不一致")
                }
                // 未注册进 ComponentRegistry 的类不是可用节点（例如 TriggerComponent 这类容器基类），
                // 标了注解也不该出现在编辑器面板里。
                if (registeredKeys.isNotEmpty() && key !in registeredKeys) {
                    unregistered.add("$className (key=\"$key\")")
                    continue
                }

                val entry = JsonObject()
                entry.addProperty("id", editorId(category, type.simpleName))
                entry.addProperty("key", key)
                entry.addProperty("name", annotationString(node, "name"))
                entry.addProperty("category", category)
                entry.addProperty("container", annotationBoolean(node, "container"))
                entry.addProperty("description", annotationString(node, "description"))
                annotationString(node, "nameZh").takeIf { it.isNotEmpty() }
                    ?.let { entry.addProperty("displayNameZh", it) }
                annotationString(node, "descriptionZh").takeIf { it.isNotEmpty() }
                    ?.let { entry.addProperty("descriptionZh", it) }

                // 前置条件：缺少时节点通常静默失效，编辑器需要据此提示
                val requires = JsonObject()
                annotationStringArray(node, "requiresPlugins").takeIf { it.isNotEmpty() }?.let { plugins ->
                    requires.add("plugins", JsonArray().apply { plugins.forEach { add(it) } })
                }
                annotationStringArray(node, "requiresNodes").takeIf { it.isNotEmpty() }?.let { keys ->
                    requires.add("nodes", JsonArray().apply { keys.forEach { add(it) } })
                }
                annotationEnumArray(node, "requiresCapabilities").takeIf { it.isNotEmpty() }?.let { caps ->
                    requires.add("capabilities", JsonArray().apply { caps.forEach { add(it) } })
                }
                if (requires.size() > 0) entry.add("requires", requires)

                val fields = JsonArray()
                for (field in type.declaredFields) {
                    if (!Modifier.isStatic(field.modifiers)) continue
                    @Suppress("UNCHECKED_CAST")
                    val meta = field.getAnnotation(fieldAnnotation as Class<Annotation>) ?: continue
                    field.isAccessible = true
                    val configKey = runCatching { field.get(null) as? String }.getOrNull() ?: continue
                    fields.add(buildField(meta, configKey))
                    fieldCount++
                }
                entry.add("fields", fields)
                nodes.add(entry)
            }
        }

        val out = outputFile.get().asFile
        out.parentFile.mkdirs()
        out.writeText(gson.toJson(nodes), Charsets.UTF_8)

        logger.lifecycle("节点导出完成：${nodes.size()} 个节点，${fieldCount} 个字段 → ${out.name}")
        if (unregistered.isNotEmpty()) {
            // 不算错误：基类带注解只是多余，但要让人看见以便清理
            logger.lifecycle("已跳过 ${unregistered.size} 个未注册的类：${unregistered.joinToString("、")}")
        }
        if (missingKey.isNotEmpty()) {
            // key 不一致会让编辑器产出插件读不懂的配置，必须让构建失败
            missingKey.forEach { logger.error("  $it") }
            throw IllegalStateException("有 ${missingKey.size} 个节点的注解 key 与 getKey() 不一致")
        }
    }

    private fun buildField(meta: Annotation, configKey: String): JsonObject {
        val obj = JsonObject()
        obj.addProperty("kind", annotationEnumName(meta, "kind"))
        obj.addProperty("key", configKey)
        obj.addProperty("label", annotationString(meta, "label"))
        annotationString(meta, "defaultValue").takeIf { it.isNotEmpty() }
            ?.let { obj.addProperty("value", it) }

        val options = annotationStringArray(meta, "options")
        if (options.isNotEmpty()) {
            obj.add("options", JsonArray().apply { options.forEach { add(it) } })
        }
        annotationString(meta, "tooltip").takeIf { it.isNotEmpty() }
            ?.let { obj.addProperty("tooltip", it) }
        annotationString(meta, "labelZh").takeIf { it.isNotEmpty() }
            ?.let { obj.addProperty("labelZh", it) }
        annotationString(meta, "tooltipZh").takeIf { it.isNotEmpty() }
            ?.let { obj.addProperty("tooltipZh", it) }

        // 编辑器期望 optionsZh 是 {value,label} 对象数组，与 options 按序对应
        val optionsZh = annotationStringArray(meta, "optionsZh")
        if (optionsZh.isNotEmpty()) {
            if (options.size != optionsZh.size) {
                throw IllegalStateException(
                    "字段 $configKey 的 options(${options.size}) 与 optionsZh(${optionsZh.size}) 数量不匹配"
                )
            }
            val pairs = JsonArray()
            options.forEachIndexed { index, value ->
                pairs.add(JsonObject().apply {
                    addProperty("value", value)
                    addProperty("label", optionsZh[index])
                })
            }
            obj.add("optionsZh", pairs)
        }
        return obj
    }

    /** BlockBreakTrigger + trigger → TriggerBlockBreak，与编辑器既有 id 保持一致。 */
    private fun editorId(category: String, simpleName: String): String {
        val prefix = category.replaceFirstChar { it.uppercase() }
        return prefix + simpleName.removeSuffix(prefix)
    }

    /**
     * 读某个节点类 getKey() 的返回值。
     *
     * 从源码读而不是反射调用：部分节点的静态初始化块引用 Bukkit 的枚举
     * （例如 LocationTarget 用 Material 建集合），构建期实例化会抛异常。
     * 返回 null 表示没解析到，调用方据此跳过校验而不是误报不一致。
     */
    private fun readGetKey(category: String, simpleName: String): String? {
        val file = project.rootProject
            .file("root/src/main/java/com/sucy/skill/dynamic/$category/$simpleName.java")
        if (!file.isFile) return null
        return Regex("""getKey\(\)\s*\{\s*return\s+"([^"]+)"""")
            .find(file.readText(Charsets.UTF_8))
            ?.groupValues?.get(1)
    }

    /**
     * 取 ComponentRegistry 里注册的全部节点 key。
     *
     * 不反射执行注册代码：那需要运行中的插件实例。改为解析源码里的
     * {@code register(new XxxComponent())} 调用，再回到编译产物问每个类的 getKey()。
     * 解析不到源码时返回空集合，调用方据此跳过该项校验而不是误报。
     */
    private fun readRegisteredKeys(classesDir: File): Set<String> {
        val source = project.rootProject
            .file("root/src/main/java/com/sucy/skill/dynamic/ComponentRegistry.java")
        if (!source.isFile) return emptySet()

        val simpleNames = Regex("""register\(new (\w+)\(\)\)""")
            .findAll(source.readText(Charsets.UTF_8))
            .map { it.groupValues[1] }
            .toSet()
        if (simpleNames.isEmpty()) return emptySet()

        // 直接读源码里的 getKey() 返回值：实例化会触发静态初始化，
        // 而部分节点（如 LocationTarget）的 static 块引用 Bukkit 的 Material，
        // 构建期没有服务器，实例化必然失败并被误判为"未注册"。
        val keys = mutableSetOf<String>()
        val getKeyPattern = Regex("""getKey\(\)\s*\{\s*return\s+"([^"]+)"""")
        for (category in listOf("trigger", "target", "condition", "mechanic")) {
            for (name in simpleNames) {
                val file = project.rootProject
                    .file("root/src/main/java/com/sucy/skill/dynamic/$category/$name.java")
                if (!file.isFile) continue
                getKeyPattern.find(file.readText(Charsets.UTF_8))
                    ?.let { keys.add(it.groupValues[1]) }
            }
        }
        return keys
    }

    private fun loadClass(loader: ClassLoader, name: String): Class<*>? =
        runCatching { Class.forName(name, false, loader) }.getOrNull()

    private fun annotationString(annotation: Annotation, method: String): String =
        runCatching { annotation.javaClass.getMethod(method).invoke(annotation) as? String ?: "" }.getOrElse { "" }

    private fun annotationBoolean(annotation: Annotation, method: String): Boolean =
        runCatching { annotation.javaClass.getMethod(method).invoke(annotation) as? Boolean ?: false }.getOrElse { false }

    private fun annotationStringArray(annotation: Annotation, method: String): List<String> =
        runCatching {
            @Suppress("UNCHECKED_CAST")
            (annotation.javaClass.getMethod(method).invoke(annotation) as? Array<String>)?.toList() ?: emptyList()
        }.getOrElse { emptyList() }

    private fun annotationEnumName(annotation: Annotation, method: String): String =
        runCatching { (annotation.javaClass.getMethod(method).invoke(annotation) as? Enum<*>)?.name ?: "" }
            .getOrElse { "" }

    private fun annotationEnumArray(annotation: Annotation, method: String): List<String> =
        runCatching {
            val value = annotation.javaClass.getMethod(method).invoke(annotation) as? Array<*>
            value?.mapNotNull { (it as? Enum<*>)?.name } ?: emptyList()
        }.getOrElse { emptyList() }
}
