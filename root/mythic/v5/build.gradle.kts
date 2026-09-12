plugins {
    `java-library`
}

repositories {
    mavenLocal()
    maven { url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") }
    maven { url = uri("https://repo.maven.apache.org/maven2/") }
}

dependencies {
    api(project(":root:mythic"))
    // MythicMobs 5 引用了 1.16 之后新增的 Bukkit 类型，需较新的 API。
    compileOnly("org.spigotmc:spigot-api:1.19.2-R0.1-SNAPSHOT")
    // MythicMobs 5.x；仅编译期需要，运行时由服务器提供。
    compileOnly(fileTree("lib"))
}

java.sourceCompatibility = JavaVersion.VERSION_1_8
java.targetCompatibility = JavaVersion.VERSION_1_8

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}
