// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    // Overrides AGP 9 built-in Kotlin (2.2.x) so it matches Zernikalos engine (2.4.0).
    alias(libs.plugins.jetbrains.kotlin.android) apply false
}