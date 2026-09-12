plugins {
    `java-library`
}

dependencies {
    // Exposed as api so the later legacy generations, which extend these
    // classes, inherit the type without re-declaring the dependency.
    api(project(":root:nms:nms-api"))
    // 绑定本世代的服务端，使 NMS 访问获得编译期检查。
    compileOnly("org.spigotmc:spigot:1.8.8-R0.1-SNAPSHOT")
}
