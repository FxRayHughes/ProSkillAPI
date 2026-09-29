plugins {
    `java-library`
}

dependencies {
    // Reuse the stable flattened implementation and override only the API
    // that first appeared in 1.14. The superclass remains safe on 1.13.
    api(project(":root:nms:nms-v1_13"))
}
