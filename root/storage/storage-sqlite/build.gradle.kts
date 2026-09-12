plugins {
    `java-library`
}

dependencies {
    api(project(":root:storage:storage-api"))
    // Xerial bundles the native SQLite library for the server platforms while
    // keeping the JDBC API usable from Java 8 bytecode.
    implementation("org.xerial:sqlite-jdbc:3.53.4.0")
}
