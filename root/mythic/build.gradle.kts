plugins {
    java
}

repositories {
    mavenLocal()
    maven { url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") }
    maven { url = uri("https://repo.maven.apache.org/maven2/") }
}

dependencies {
    // 接口层只认 Bukkit 类型，刻意不引入任何 MythicMobs 依赖。
    compileOnly("org.spigotmc:spigot-api:1.16.4-R0.1-SNAPSHOT")
}

java.sourceCompatibility = JavaVersion.VERSION_1_8
java.targetCompatibility = JavaVersion.VERSION_1_8

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}
