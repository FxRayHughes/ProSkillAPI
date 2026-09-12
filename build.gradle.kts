/*
 * 仓库根不产出任何构件，全部代码位于 root/ 子项目及其下属模块。
 * 公共配置由 root/build.gradle.kts 的 subprojects 块负责，
 * 那里能同时覆盖 nms、mythic、storage 等全部实现模块。
 */

plugins {
    base
}

// 导出 new-editor 所需的节点元数据与枚举表（含中文翻译）。
apply<proskill.export.EditorExportPlugin>()

group = "com.sucy.skill"
description = "ProSkillAPI"

// 让根目录的 build 直接产出插件 jar，保持既有的构建命令不变。
tasks.named("build") {
    dependsOn(":root:build")
}
