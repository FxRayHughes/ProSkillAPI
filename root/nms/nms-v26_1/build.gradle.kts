plugins {
    `java-library`
}

dependencies {
    api(project(":root:nms:nms-api"))
    // 26.x 去掉了 CraftBukkit 版本段（org.bukkit.craftbukkit.entity.CraftPlayer），
    // 两个小版本无法靠包名区分，只能各自绑定对应的服务端 jar。
    compileOnly("org.spigotmc:spigot:26.1.2-R0.1-SNAPSHOT")
}
