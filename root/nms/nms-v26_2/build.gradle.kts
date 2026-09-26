plugins {
    `java-library`
}

dependencies {
    api(project(":root:nms:nms-api"))
    // 26.x has no CraftBukkit version segment; pin the locally built server
    // artifact so this generation cannot resolve against another minor release.
    compileOnly("org.spigotmc:spigot:26.2-R0.1-SNAPSHOT")
}
