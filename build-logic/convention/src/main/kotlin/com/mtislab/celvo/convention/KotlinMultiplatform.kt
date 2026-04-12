package com.mtislab.celvo.convention


import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

internal fun Project.configureKotlinMultiplatform() {
    extensions.configure<LibraryExtension> {
        namespace = this@configureKotlinMultiplatform.pathToPackageName()
    }

    configureAndroidTarget()

    extensions.configure<KotlinMultiplatformExtension> {
        // Register iOS targets (always needed for all KMP library modules)
        iosX64()
        iosArm64()
        iosSimulatorArm64()

        compilerOptions {
            freeCompilerArgs.add("-Xexpect-actual-classes")
            freeCompilerArgs.add("-opt-in=kotlin.RequiresOptIn")
            freeCompilerArgs.add("-opt-in=kotlin.time.ExperimentalTime")
        }
    }

    // Configure framework binaries AFTER all plugins are applied.
    // If the CocoaPods plugin is present, it manages its own framework binaries,
    // so we skip manual framework configuration to avoid conflicts.
    afterEvaluate {
        if (!pluginManager.hasPlugin("org.jetbrains.kotlin.native.cocoapods")) {
            extensions.configure<KotlinMultiplatformExtension> {
                listOf(
                    iosX64(),
                    iosArm64(),
                    iosSimulatorArm64()
                ).forEach { iosTarget ->
                    iosTarget.binaries.framework {
                        baseName = this@afterEvaluate.pathToFrameworkName()
                    }
                }
            }
        }
    }
}