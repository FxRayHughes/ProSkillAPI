plugins {
    `java-library`
}

dependencies {
    // Exposed as api because every later modern generation extends this bridge.
    api(project(":root:nms:nms-api"))
    // BuildTools supplies this full server artifact through mavenLocal; pinning
    // the generation prevents an unprepared local build from passing silently.
    compileOnly("org.spigotmc:spigot:1.13.2-R0.1-SNAPSHOT")
}
