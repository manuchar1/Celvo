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
                implementation(projects.core.domain)
                implementation(libs.bundles.ktor.common)
                implementation(libs.koin.core)
                implementation(libs.touchlab.kermit)
                implementation(libs.supabase.auth)
                implementation(libs.datastore)
                implementation(libs.datastore.preferences)
            }
        }



        androidMain {
            dependencies {
               implementation(libs.ktor.client.okhttp)
                implementation(libs.koin.android)
                implementation(libs.google.pay.wallet)
                implementation(libs.kotlinx.coroutines.play.services)
                // For Google Pay config (single source of truth shared with the
                // Compose PayButton launcher). Scoped to androidMain only.
                implementation(projects.core.designsystem)
            }
        }



        iosMain {
            dependencies {
                implementation(libs.ktor.client.darwin)
            }
        }
    }

}