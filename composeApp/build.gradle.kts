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


            implementation(libs.koin.android)

            implementation(projects.feature.auth.presentation)


        }
        commonMain.dependencies {

            implementation(project(":core:data"))
            implementation(project(":core:domain"))
            implementation(project(":core:designsystem"))
            implementation(project(":core:presentation"))

            implementation(project(":feature:auth:domain"))
            implementation(project(":feature:auth:presentation"))

            implementation(project(":feature:chat:data"))
            implementation(project(":feature:chat:database"))
            implementation(project(":feature:chat:domain"))
            implementation(project(":feature:chat:presentation"))

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





        }
        
    }
    sourceSets.commonTest.dependencies {
        implementation(kotlin("test"))
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
