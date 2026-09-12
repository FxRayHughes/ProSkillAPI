plugins {
    `kotlin-dsl`
}

repositories {
    mavenLocal()
    // 与主工程保持一致的国内镜像，避免构建期走境外源。
    maven {
        url = uri("https://maven.aliyun.com/repository/public")
        metadataSources {
            mavenPom()
            artifact()
            ignoreGradleMetadataRedirection()
        }
    }
    maven {
        url = uri("https://mirrors.tuna.tsinghua.edu.cn/maven2/")
        metadataSources {
            mavenPom()
            artifact()
            ignoreGradleMetadataRedirection()
        }
    }
    mavenCentral()
}

dependencies {
    // 解析 Mojang 的 version_manifest 与语言文件，以及生成编辑器 JSON
    implementation("com.google.code.gson:gson:2.10.1")
    // Bukkit 枚举的静态初始化依赖 Guava，反射读取时必须提供，
    // 否则 Material/Sound 会抛 NoClassDefFoundError 而被读成空表。
    implementation("com.google.guava:guava:32.1.3-jre")
}
