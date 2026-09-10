plugins {
    `java-library`
}

dependencies {
    // Exposed as api because every later modern generation extends this bridge.
    api(project(":nms:nms-api"))
}
