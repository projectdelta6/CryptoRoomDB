// Top-level build file where you can add configuration options common to all sub-projects/modules.

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    alias(libs.plugins.kotlinKSP) apply false
    alias(libs.plugins.kotlin.parcelize) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.room) apply false
}

// Ensure all projects have access to the repositories
//allprojects {
//    repositories {
//        google()
//        mavenCentral()
//        maven(url = "https://jitpack.io")
//    }
//}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}

tasks.wrapper {
    gradleVersion = "8.5.0"
    distributionType = Wrapper.DistributionType.ALL
}
