buildscript {
    dependencies {
        classpath(libs.hilt.android.gradle.plugin)
    }
}// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    kotlin("jvm") version "2.1.10" apply false
    id("com.android.application") version "8.6.1" apply false
    id("org.jetbrains.kotlin.android") version "2.1.10" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.0"
    id("org.jetbrains.kotlin.kapt") version "2.1.0"
    id("com.google.gms.google-services") version "4.4.3" apply false

    // ⬇️ Hilt plugin disponible para los módulos (no se aplica aquí)
    id("com.google.dagger.hilt.android") version "2.51.1" apply false
}
