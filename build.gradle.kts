// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
}

plugins {
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.devtools.ksp) apply false
    alias(libs.plugins.compose.compiler) apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}