package proskill.export

import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File

/**
 * 把插件源码与各版本 spigot jar 里的数据导出给 new-editor。
 *
 * 编辑器此前的节点数据是从旧编辑器的 JS 反向抽取的，插件加了节点编辑器并不知道；
 * 本插件把数据源倒转过来，并在导出过程中完成中文翻译。
 */
class EditorExportPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val generated = project.rootProject.file("new-editor/src/features/generated")

        // lambda 必须写在括号内：register 有 vararg 重载，尾随 lambda 会解析失败。
        // Action 是带 receiver 的函数式接口，因此用 this 而非具名参数。
        project.tasks.register("exportEnums", ExportEnumsTask::class.java, Action<ExportEnumsTask> {
            group = "proskill export"
            description = "导出各版本的 Material / Sound / Particle 表（含中文）"
            outputDir.set(File(generated, "enums"))
            languageCache.set(project.rootProject.file("buildSrc/.cache/lang").absolutePath)
            dictionaryPath.set(project.rootProject.file("buildSrc/zh-dict.json").absolutePath)
        })

        // 节点元数据来自主工程的编译产物，因此在 :root 上注册并依赖其 classes。
        project.rootProject.project(":root").let { root ->
            root.tasks.register("exportNodes", ExportNodesTask::class.java, Action<ExportNodesTask> {
                group = "proskill export"
                description = "从 @SkillNode / @SkillField 注解导出节点元数据（含中文）"
                dependsOn(root.tasks.named("classes"))
                // 用 compileClasspath 而非 runtimeClasspath：Bukkit 是 compileOnly 依赖，
                // 不在运行时路径里，而节点类的签名引用 Bukkit 类型，加载时必须能解析。
                root.extensions.getByType(org.gradle.api.plugins.JavaPluginExtension::class.java)
                    .sourceSets.getByName("main").let { main ->
                        classpath.from(main.output.classesDirs)
                        classpath.from(main.compileClasspath)
                    }
                outputFile.set(File(generated, "nodes.generated.json"))
            })
        }
    }
}
