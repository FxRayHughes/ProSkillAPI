plugins {
    java
}

dependencies {
    // DragonCore mechanics extend the plugin's existing dynamic component base
    // classes, but the main module loads this jar reflectively only when the
    // DragonCore plugin is present. Compiling against main output avoids a
    // runtime dependency cycle between the plugin jar and optional module jar.
    compileOnly(rootProject.extensions.getByType<SourceSetContainer>()["main"].output)
}
