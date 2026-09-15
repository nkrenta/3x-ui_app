// Top-level build file where you can add configuration options common to all sub-projects/modules.
// Корневой build.gradle.kts
plugins {
    id("com.android.application") version "9.4.0" apply false
    id("org.jetbrains.kotlin.android") version "2.2.10" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.10" apply false

    // Если используете Version Catalogs (libs.versions.toml):
    // alias(libs.plugins.kotlin.android) apply false
}