import com.android.build.api.dsl.ApplicationExtension
import com.mtislab.celvo.convention.configureKotlinAndroid

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import java.io.FileInputStream
import java.util.Properties

class AndroidApplicationConventionPlugin: Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.application")
            }

            // Release signing is driven by a non-committed keystore.properties at
            // the repo root (see keystore.properties.example). When it is absent
            // — e.g. on a fresh checkout or CI without secrets — the release build
            // simply stays unsigned instead of failing configuration.
            val keystorePropsFile = rootProject.file("keystore.properties")
            val hasKeystore = keystorePropsFile.exists()
            val keystoreProps = Properties().apply {
                if (hasKeystore) FileInputStream(keystorePropsFile).use { load(it) }
            }

            extensions.configure<ApplicationExtension> {
                namespace = "com.mtislab.celvo"

                defaultConfig {
                    applicationId = "com.mtislab.celvo"
                    targetSdk = 36
                    versionCode = 2
                    versionName = "1.1"
                }
                packaging {
                    resources {
                        excludes += "/META-INF/{AL2.0,LGPL2.1}"
                    }
                }

                if (hasKeystore) {
                    signingConfigs {
                        create("release") {
                            storeFile = rootProject.file(keystoreProps.getProperty("storeFile"))
                            storePassword = keystoreProps.getProperty("storePassword")
                            keyAlias = keystoreProps.getProperty("keyAlias")
                            keyPassword = keystoreProps.getProperty("keyPassword")
                        }
                    }
                }

                buildTypes {
                    getByName("release") {
                        isMinifyEnabled = false
                        if (hasKeystore) {
                            signingConfig = signingConfigs.getByName("release")
                        }
                    }
                }

                configureKotlinAndroid(this)
            }
        }
    }
}