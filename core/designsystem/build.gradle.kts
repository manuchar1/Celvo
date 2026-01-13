plugins {
    alias(libs.plugins.convention.cmp.library)
}

kotlin {
    jvm()

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlin.stdlib)
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)

                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)
                implementation(libs.coil.compose)
            }
        }

        androidMain {
            dependencies {
                implementation(compose.uiTooling)
            }
        }

        iosMain {
            dependencies {
                // iOS dependencies
            }
        }
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = "com.celvo.core.designsystem.resources"

}