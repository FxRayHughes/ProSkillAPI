package proskill.export

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.net.URL
import java.net.URLClassLoader

/**
 * 从各版本的 spigot jar 导出 Material / Sound / Particle 三张表，并附中文翻译。
 *
 * 每个版本用独立的 URLClassLoader 加载，避免不同世代的同名枚举互相污染。
 * jar 来自本地 .m2（由 BuildTools 生成），缺哪个版本就跳过哪个并在报告中列出。
 */
abstract class ExportEnumsTask : DefaultTask() {

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    /** 语言文件缓存目录，避免每次构建重复下载。 */
    @get:Input
    abstract val languageCache: Property<String>

    /** 补充词典路径，用于补官方语言文件的缺口。 */
    @get:Input
    abstract val dictionaryPath: Property<String>

    @TaskAction
    fun export() {
        // 用 spigot-api 而非 spigot：后者从 1.18 起是 bootstrap 启动器，
        // 内部不含 org.bukkit.Material，反射只会读到空表。
        val m2 = File(System.getProperty("user.home"), ".m2/repository/org/spigotmc/spigot-api")
        if (!m2.isDirectory) {
            logger.warn("未找到本地 spigot-api 仓库: $m2，跳过枚举导出")
            return
        }

        val out = outputDir.get().asFile
        out.mkdirs()
        val cacheDir = File(languageCache.get())
        val dictionary = EnumTranslator.loadDictionary(File(dictionaryPath.get()))
        val gson = GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()

        val exported = mutableListOf<String>()
        val skipped = mutableListOf<String>()
        val index = JsonArray()

        m2.listDirectories().sortedWith(VERSION_ORDER).forEach { dir ->
            val version = dir.name.removeSuffix("-R0.1-SNAPSHOT")
            val jar = dir.listFiles { f ->
                f.name.startsWith("spigot-api-") && f.name.endsWith(".jar") &&
                    !f.name.contains("sources") && !f.name.contains("remapped")
            }?.firstOrNull()
            if (jar == null) {
                skipped.add("$version（无 jar）")
                return@forEach
            }

            val vanilla = MojangLanguage.fetch(version, cacheDir)
            val translator = EnumTranslator(vanilla, dictionary)
            val payload = readEnums(jar, version, translator)
            if (payload == null) {
                skipped.add("$version（读取失败）")
                return@forEach
            }

            File(out, "$version.json").writeText(gson.toJson(payload), Charsets.UTF_8)
            exported.add(version)
            index.add(version)

            val counts = listOf("materials", "sounds", "particles")
                .joinToString(" ") { "$it=${payload.getAsJsonArray(it).size()}" }
            val missed = translator.missing.entries.joinToString(" ") { "${it.key}未译=${it.value.size}" }
            logger.lifecycle("  $version  $counts  ${if (missed.isEmpty()) "全部已译" else missed}")
            // 未命中清单写到文件，方便按需补词典
            if (translator.missing.isNotEmpty()) {
                val report = JsonObject()
                translator.missing.forEach { (cat, names) ->
                    report.add(cat, JsonArray().apply { names.forEach { add(it) } })
                }
                File(out, "$version.untranslated.json").writeText(gson.toJson(report), Charsets.UTF_8)
            }
        }

        File(out, "index.json").writeText(gson.toJson(index), Charsets.UTF_8)
        logger.lifecycle("枚举导出完成：${exported.size} 个版本")
        if (skipped.isNotEmpty()) {
            // 明确列出跳过项，避免"少了版本却没人发现"
            logger.warn("跳过 ${skipped.size} 个：${skipped.joinToString("、")}")
        }
    }

    /** 用隔离的类加载器反射读取三个枚举。 */
    private fun readEnums(jar: File, version: String, translator: EnumTranslator): JsonObject? {
        return runCatching {
            // 父加载器指向本任务自己的加载器：Bukkit 枚举的静态初始化需要 Guava，
            // 完全切断父链会让 Material/Sound 抛 NoClassDefFoundError 而读成空表。
            // 同时 spigot-api 自身的类仍优先从 jar 解析，不同版本互不干扰。
            URLClassLoader(arrayOf<URL>(jar.toURI().toURL()), javaClass.classLoader).use { loader ->
                val root = JsonObject()
                root.addProperty("version", version)
                root.add("materials", collect(loader, "org.bukkit.Material") { translator.translateMaterial(it) })
                root.add("sounds", collect(loader, "org.bukkit.Sound") { translator.translateSound(it) })
                root.add("particles", collect(loader, "org.bukkit.Particle") { translator.translateParticle(it) })
                root
            }
        }.getOrElse {
            logger.warn("  $version 枚举读取失败: ${it.message}")
            null
        }
    }

    /**
     * 读取一个常量容器里的全部常量名。
     *
     * 不能用 values() 反射：1.21.3 起 Sound 从 enum 变成了 interface，其常量经
     * Bukkit.getRegistry() 初始化，构建期没有运行中的服务器，静态初始化必定抛
     * NullPointerException。改为以 initialize=false 加载后直接枚举 public static 字段，
     * 对 enum 与 interface 两种形态都成立。
     */
    private fun collect(loader: ClassLoader, className: String, translate: (String) -> String?): JsonArray {
        val array = JsonArray()
        val type = runCatching { Class.forName(className, false, loader) }.getOrElse {
            // 读不到要说出来，否则空表会被误当成"该版本没有这个枚举"
            logger.warn("    $className 加载失败: ${it.javaClass.simpleName} ${it.message}")
            return array
        }

        val names = type.declaredFields
            .filter { field ->
                java.lang.reflect.Modifier.isStatic(field.modifiers) &&
                    java.lang.reflect.Modifier.isPublic(field.modifiers) &&
                    // 排除 $VALUES 之类的合成字段
                    !field.isSynthetic &&
                    // 常量的类型就是容器自身（enum 常量与 interface 上的 Sound 字段皆然）
                    field.type == type
            }
            .map { it.name }

        if (names.isEmpty()) {
            logger.warn("    $className 没有读到任何常量")
            return array
        }

        names.forEach { name ->
            val entry = JsonObject()
            entry.addProperty("name", name)
            translate(name)?.let { entry.addProperty("zh", it) }
            array.add(entry)
        }
        return array
    }

    private fun File.listDirectories(): List<File> =
        listFiles()?.filter { it.isDirectory } ?: emptyList()

    private companion object {
        /** 1.9.4 要排在 1.12.2 前，26.x 要排在 1.x 后，纯字典序做不到。 */
        val VERSION_ORDER = Comparator<File> { a, b ->
            fun parts(f: File) = f.name.removeSuffix("-R0.1-SNAPSHOT")
                .split('.').map { it.toIntOrNull() ?: 0 }
            val x = parts(a); val y = parts(b)
            var result = 0
            for (i in 0 until maxOf(x.size, y.size)) {
                result = (x.getOrElse(i) { 0 }).compareTo(y.getOrElse(i) { 0 })
                if (result != 0) break
            }
            result
        }
    }
}
