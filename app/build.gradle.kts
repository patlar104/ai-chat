plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.example.aicompanion"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.aicompanion"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:ui"))
    implementation(project(":core:data"))
    implementation(project(":core:network"))
    implementation(project(":core:audio"))
    implementation(project(":core:ai"))
    implementation(project(":core:automation"))

    implementation(project(":feature:voice"))
    implementation(project(":feature:chat"))
    implementation(project(":feature:avatar"))
    implementation(project(":feature:memory"))
    implementation(project(":feature:homecontrol"))
    implementation(project(":feature:tasks"))
    implementation(project(":feature:settings"))

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)

    implementation(libs.activity.compose)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.navigation.compose)
    implementation(libs.hilt.navigation.compose)

    implementation(libs.timber)
    implementation(libs.coroutines.android)
}
