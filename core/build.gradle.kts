plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
}

tasks.test {
    useJUnitPlatform()
}

// Architectural rule: :core must remain Android-free.
// Fails the build on any `import android.` in core/src/main.
tasks.register("checkNoAndroidImports") {
    group = "verification"
    description = "Fails if :core sources import android.* APIs."

    val sources = fileTree("src/main/kotlin") { include("**/*.kt") }
    inputs.files(sources)

    doLast {
        val violations = sources.files.filter { file ->
            file.readLines().any { line ->
                val trimmed = line.trim()
                trimmed.startsWith("import android.") ||
                    trimmed.startsWith("import androidx.")
            }
        }
        check(violations.isEmpty()) {
            "Forbidden Android imports in :core:\n" +
                violations.joinToString("\n") { "  - ${it.relativeTo(projectDir)}" }
        }
    }
}

tasks.named("check") {
    dependsOn("checkNoAndroidImports")
}
