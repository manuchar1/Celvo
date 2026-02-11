plugins {
    id("com.mtislab.convention.cmp.feature")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.mtislab.celvo.feature.myesim"
}

kotlin {
    jvm()
}

dependencies {
    commonMainImplementation(projects.core.data)
    commonMainImplementation(projects.core.domain)
    commonMainImplementation(libs.coil.compose)
    commonMainImplementation(libs.coil.network.ktor)
    commonMainImplementation(projects.core.designsystem)
    commonMainImplementation(compose.components.resources)
}