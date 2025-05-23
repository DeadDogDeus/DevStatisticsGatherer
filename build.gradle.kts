plugins {
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
}

tasks.register("runAll") {
    dependsOn(":backend:run", ":composeApp:wasmJsBrowserDevelopmentRun")
}