plugins {
    `java-library`
}

dependencies {
    api(project(":root:nms:nms-api"))
    // 绑定本世代的服务端，使 NMS 访问获得编译期检查。
    compileOnly("org.spigotmc:spigot:1.9.4-R0.1-SNAPSHOT")
}
