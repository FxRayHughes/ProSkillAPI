# 本地构建

本项目的 NMS 模块按服务端版本声明 `org.spigotmc:spigot` 编译依赖。完整 Spigot 服务端 JAR 不在公开 Maven 仓库分发；必须先用[官方 BuildTools](https://www.spigotmc.org/wiki/buildtools/)构建并安装到本机 `~/.m2/repository`，不能只添加 Spigot 仓库或用 `spigot-api` 替代。

1. 通过 `mise` 准备 Java 8、17、21 和 25，以及 Git。BuildTools 应在**仓库外**的独立工作目录运行；不要把生成的服务端 JAR 提交或上传到本项目。
2. 从[官方 Jenkins](https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar)下载 `BuildTools.jar`。对下表每个版本运行 `mise exec java@<对应 JDK> -- java -jar /path/to/BuildTools.jar --rev <版本>`；同一工作目录可以依次构建多个版本，也可以每个版本使用独立目录。
3. 确认 `~/.m2/repository/org/spigotmc/spigot/` 下存在对应版本的 JAR，然后在仓库根目录执行 `mise exec java@25 -- sh ./gradlew clean build`。`root/build/libs/` 中的插件 JAR 是发布产物。注意 BuildTools 将 `1.21.11` 发布为 `1.21.11-R0.2-SNAPSHOT`，其他下表版本为 `R0.1-SNAPSHOT`。

| BuildTools `--rev` | JDK |
| --- | --- |
| `1.8.8`, `1.9.4`, `1.10.2`, `1.11.2`, `1.12.2`, `1.13.2`, `1.16.5` | 8 |
| `1.17.1` | 17 |
| `1.20.6`, `1.21.11` | 21 |
| `26.1.2`, `26.2` | 25 |

Gradle 使用 Java 25 运行，但项目编译选项仍生成 Java 8 字节码。`root/lib/` 下的第三方插件 JAR 也参与编译，构建前需保留。若只安装部分 Spigot 版本，完整 `build` 会在对应 NMS 模块解析依赖时失败。
