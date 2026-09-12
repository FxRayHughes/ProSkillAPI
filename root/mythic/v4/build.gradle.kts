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
    compileOnly("org.spigotmc:spigot-api:1.16.4-R0.1-SNAPSHOT")
    // MythicMobs 4.x；仅编译期需要，运行时由服务器提供。
    compileOnly(fileTree("lib"))
}

java.sourceCompatibility = JavaVersion.VERSION_1_8
java.targetCompatibility = JavaVersion.VERSION_1_8

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}
