
// Declare plugins but don't apply immediately (apply false) to share them across modules
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.ksp)  apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.hilt.gradle) apply false
}