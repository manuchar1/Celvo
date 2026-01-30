plugins {
    alias(libs.plugins.convention.cmp.feature)
    alias(libs.plugins.convention.buildkonfig)


}

kotlin {


    jvm()

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlin.stdlib)

                implementation(projects.feature.auth.domain)
                implementation(projects.core.domain)
                implementation(projects.core.data)
                implementation(projects.core.designsystem)
                implementation(projects.core.presentation)

                implementation(libs.bundles.koin.common)

                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)
            }
        }



        androidMain {
            dependencies {
                implementation(libs.androidx.credentials)
                implementation(libs.androidx.credentials.play.services.auth)
                implementation(libs.googleid)

            }
        }



        iosMain {
            dependencies {
                // Add iOS-specific dependencies here. This a source set created by Kotlin Gradle
                // Plugin (KGP) that each specific iOS target (e.g., iosX64) depends on as
                // part of KMP’s default source set hierarchy. Note that this source set depends
                // on common by default and will correctly pull the iOS artifacts of any
                // KMP dependencies declared in commonMain.
            }
        }
    }

}