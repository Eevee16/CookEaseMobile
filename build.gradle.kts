plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    id("com.google.gms.google-services") version "4.4.1" apply false
    id("androidx.navigation.safeargs.kotlin") version "2.7.7" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "2.2.10" apply false
}