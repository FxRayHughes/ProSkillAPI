plugins {
    `java-library`
}

dependencies {
    api(project(":serialization:serialization-api"))

    // Gson 2.14.0 is the current stable Gson release and its classes remain
    // Java 8 compatible, matching the plugin's legacy server bytecode target.
    implementation("com.google.code.gson:gson:2.14.0")
    // DataSection is used only as an in-memory migration bridge. It is never
    // serialized through its YAML methods, and MCCore remains a plugin runtime dependency.
    compileOnly(files("$rootDir/lib/MCCore-3.0.jar"))
}
