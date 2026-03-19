// Top-level build file — plugin declarations only. Module-specific config in each module's build.gradle.kts.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.ktlint) apply false
}

// ARCHITECTURAL RULE: Feature modules must NOT depend on other feature modules.
// Violation causes an immediate build configuration failure.
subprojects {
    val currentPath = path
    if (currentPath.startsWith(":feature:")) {
        afterEvaluate {
            configurations.configureEach {
                if (name.contains("Implementation", ignoreCase = true)) {
                    dependencies.withType(org.gradle.api.artifacts.ProjectDependency::class.java).configureEach {
                        // In Gradle 9, use path property directly on ProjectDependency
                        val depPath = this.path
                        if (depPath.startsWith(":feature:")) {
                            throw GradleException(
                                "Dependency violation: $currentPath -> $depPath is BANNED. " +
                                "Feature modules may only depend on :core:* modules. " +
                                "Move shared types to :core:domain."
                            )
                        }
                    }
                }
            }
        }
    }
}

// Apply detekt at root level for project-wide analysis
apply(plugin = "io.gitlab.arturbosch.detekt")

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    config.setFrom(files("detekt.yml"))
    buildUponDefaultConfig = true
    setSource(files(projectDir))
    include("**/*.kt", "**/*.kts")
    exclude("**/build/**", "**/generated/**")
    reports {
        html.required.set(true)
        xml.required.set(false)
        txt.required.set(false)
    }
}
