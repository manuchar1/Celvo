plugins {
    alias(libs.plugins.convention.kmp.library)
    alias(libs.plugins.convention.buildkonfig)

}

kotlin {

    jvm()

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlin.stdlib)
                implementation(project(":core:domain"))
                implementation(libs.bundles.ktor.common)
                implementation(libs.koin.core)
                implementation(libs.touchlab.kermit)
                implementation(libs.supabase.auth)


            }
        }



        androidMain {
            dependencies {
               implementation(libs.ktor.client.okhttp)
            }
        }



        iosMain {
            dependencies {
                implementation(libs.ktor.client.darwin)
            }
        }
    }

}