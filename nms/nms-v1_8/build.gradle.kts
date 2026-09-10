plugins {
    `java-library`
}

dependencies {
    // Exposed as api so the later legacy generations, which extend these
    // classes, inherit the type without re-declaring the dependency.
    api(project(":nms:nms-api"))
}
