plugins {
    `java-library`
}

dependencies {
    api(project(":root:nms:nms-api"))
    // BuildTools supplies this full server artifact through mavenLocal; pinning
    // the generation prevents an unprepared local build from passing silently.
    compileOnly("org.spigotmc:spigot:1.10.2-R0.1-SNAPSHOT")
}
