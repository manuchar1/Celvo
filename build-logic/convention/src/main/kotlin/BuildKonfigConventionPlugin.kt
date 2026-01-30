import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import com.codingfeline.buildkonfig.compiler.FieldSpec
import com.codingfeline.buildkonfig.gradle.BuildKonfigExtension
import com.mtislab.celvo.convention.pathToPackageName
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class BuildKonfigConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.codingfeline.buildkonfig")
            }

            extensions.configure<BuildKonfigExtension> {
                packageName = target.pathToPackageName()
                defaultConfigs {
                    val properties = gradleLocalProperties(rootDir, rootProject.providers)
                    val apiKey = properties.getProperty("API_KEY")
                        ?: ""
                    buildConfigField(FieldSpec.Type.STRING, "API_KEY", apiKey)
                    val supabaseUrl = properties.getProperty("SUPABASE_URL")
                        ?: throw IllegalStateException("Missing SUPABASE_URL in local.properties")
                    buildConfigField(FieldSpec.Type.STRING, "SUPABASE_URL", supabaseUrl)
                    val supabaseKey = properties.getProperty("SUPABASE_KEY")
                        ?: throw IllegalStateException("Missing SUPABASE_KEY in local.properties")
                    buildConfigField(FieldSpec.Type.STRING, "SUPABASE_KEY", supabaseKey)

                    val googleClientId = properties.getProperty("GOOGLE_WEB_CLIENT_ID") ?: ""
                    buildConfigField(FieldSpec.Type.STRING, "GOOGLE_WEB_CLIENT_ID", googleClientId)
                }
            }
        }
    }
}