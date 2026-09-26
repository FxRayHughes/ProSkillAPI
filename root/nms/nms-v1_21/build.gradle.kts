plugins {
    `java-library`
}

dependencies {
    api(project(":root:nms:nms-api"))
    // BuildTools publishes 1.21.11 as R0.2 (not R0.1) in mavenLocal; use its
    // actual server coordinate so this generation resolves reproducibly.
    compileOnly("org.spigotmc:spigot:1.21.11-R0.2-SNAPSHOT")
}
