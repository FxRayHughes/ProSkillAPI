plugins {
    `java-library`
}

dependencies {
    api(project(":storage:storage-api"))
    // HikariCP 5.x is the last Java 8-compatible line and provides a bounded
    // connection pool for remote SQL without tying the plugin to MCCore's
    // legacy MySQL-only helper.
    implementation("com.zaxxer:HikariCP:5.1.0")
    // Connector/J 8.4 is the Java 8-compatible MySQL LTS driver and also serves
    // MariaDB deployments that accept it.
    implementation("com.mysql:mysql-connector-j:8.4.0")
    // pgjdbc 42.7.x is the last line that still targets Java 8.
    implementation("org.postgresql:postgresql:42.7.4")
}
