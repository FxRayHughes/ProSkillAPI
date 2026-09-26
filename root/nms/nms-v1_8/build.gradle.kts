plugins {
    `java-library`
}

dependencies {
    // Exposed as api so the later legacy generations, which extend these
    // classes, inherit the type without re-declaring the dependency.
    api(project(":root:nms:nms-api"))
    // BuildTools installs this full server artifact into mavenLocal. Pinning it
    // makes a missing generation-specific server dependency fail the build.
    compileOnly("org.spigotmc:spigot:1.8.8-R0.1-SNAPSHOT")
}
