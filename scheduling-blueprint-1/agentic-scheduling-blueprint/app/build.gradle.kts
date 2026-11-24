plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android") version "2.0.20"
}

android {
    namespace = "com.gulserakh.sweets"

    compileSdk = 35

    defaultConfig {
        applicationId = "com.gulserakh.sweets"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        // Initial admin credentials — injected from the environment at build time
        // (NOT hardcoded). Usage:
        //   CAKE_ADMIN_USERNAME=admin CAKE_ADMIN_PASSWORD=secret ./gradlew assembleDebug
        buildConfigField("String", "ADMIN_USERNAME", "\"${System.getenv("CAKE_ADMIN_USERNAME") ?: "admin"}\"")
        buildConfigField("String", "ADMIN_INITIAL_PASSWORD", "\"${System.getenv("CAKE_ADMIN_PASSWORD") ?: ""}\"")
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.compose.material3:material3:1.2.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
}
