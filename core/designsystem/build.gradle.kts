plugins {
    alias(libs.plugins.convention.cmp.library)
    alias(libs.plugins.convention.buildkonfig)

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

                implementation(projects.core.domain)

            }
        }

        androidMain {
            dependencies {
                implementation(compose.uiTooling)
                implementation(libs.androidx.credentials)
                implementation(libs.androidx.credentials.play.services.auth)
                implementation(libs.googleid)
                implementation(libs.androidx.browser)
                implementation(libs.google.pay.compose.button)
                implementation(libs.google.pay.wallet)
                implementation(libs.kotlinx.coroutines.play.services)
                implementation(libs.androidx.activity.compose)
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