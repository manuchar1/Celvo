import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.convention.cmp.application)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {


    jvm()
    
    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.core.splashscreen)
            implementation(libs.koin.android)
            implementation(projects.feature.auth.presentation)


        }
        commonMain.dependencies {

            implementation(projects.core.data)
            api(projects.core.domain)
            implementation(project(":core:designsystem"))
            implementation(project(":core:presentation"))
            implementation(project(":feature:auth:domain"))
            implementation(project(":feature:auth:presentation"))
            implementation(project(":feature:chat:data"))
            implementation(project(":feature:chat:database"))
            implementation(project(":feature:chat:domain"))
            implementation(project(":feature:chat:presentation"))

            implementation(projects.feature.store)
            implementation(projects.feature.profile)
            implementation(projects.feature.myesim)

            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)

            implementation(libs.jetbrains.compose.navigation)

            implementation(libs.koin.compose.viewmodel)

            implementation(libs.supabase.auth)

            // Coil + an explicit Ktor engine so the singleton ImageLoader can
            // load network images on iOS (see App.kt). coil.network.ktor pulls
            // in the Ktor network fetcher; ktor.client.core provides HttpClient.
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor)
            implementation(libs.ktor.client.core)





        }
        
    }
    sourceSets.commonTest.dependencies {
        implementation(kotlin("test"))
    }





    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>().configureEach {
        if (konanTarget.family.isAppleFamily) {
            binaries.withType<org.jetbrains.kotlin.gradle.plugin.mpp.Framework>().configureEach {
                export(projects.core.domain)
            }
        }
    }

}





compose.desktop {
    application {
        mainClass = "com.mtislab.celvo.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.mtislab.celvo"
            packageVersion = "1.0.0"
        }
    }
}
