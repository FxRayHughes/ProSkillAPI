plugins {
    `java-library`
}

dependencies {
    api(project(":root:serialization:serialization-api"))

    // Gson 2.14.0 is the current stable Gson release and its classes remain
    // Java 8 compatible, matching the plugin's legacy server bytecode target.
    implementation("com.google.code.gson:gson:2.14.0")
    // DataSection is used only as an in-memory migration bridge. It is never
    // serialized through its YAML methods, and MCCore remains a plugin runtime dependency.
    // lib/ 随主工程位于 root/ 子项目下，rootDir 指向的是不含代码的仓库根。
    compileOnly(files("$rootDir/root/lib/MCCore-3.0.jar"))
}
