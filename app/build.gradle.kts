import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    id("jacoco")
}

android {
    namespace = "ai.ki_kompetenz_training_org"  // Now matches Google Play requirement
    compileSdk = 35

    defaultConfig {
        applicationId = "ai.ki_kompetenz_training_org.free"
        minSdk = 26
        targetSdk = 35
        versionCode = 8
        versionName = "1.1.0"

        // API base URL: override with -PapiBaseUrl=https://... on the command line.
        val apiBaseUrl = (project.findProperty("apiBaseUrl") as String?) ?: "https://ki-kompetenz-training.org"
        buildConfigField("String", "API_BASE_URL", "\"$apiBaseUrl\"")
    }

    signingConfigs {
        // Read keystore settings from environment or gradle properties
        val storeFile = providers.gradleProperty("KIKOMPETENZ_RELEASE_STORE_FILE")
            .orElse(providers.environmentVariable("KIKOMPETENZ_RELEASE_STORE_FILE"))
            .orNull
        val storePassword = providers.gradleProperty("KIKOMPETENZ_RELEASE_STORE_PASSWORD")
            .orElse(providers.environmentVariable("KIKOMPETENZ_RELEASE_STORE_PASSWORD"))
            .orNull
        val keyAlias = providers.gradleProperty("KIKOMPETENZ_RELEASE_KEY_ALIAS")
            .orElse(providers.environmentVariable("KIKOMPETENZ_RELEASE_KEY_ALIAS"))
            .orNull
        val keyPassword = providers.gradleProperty("KIKOMPETENZ_RELEASE_KEY_PASSWORD")
            .orElse(providers.environmentVariable("KIKOMPETENZ_RELEASE_KEY_PASSWORD"))
            .orNull
        
        // Create release signing config if all values are provided
        if (
            !storeFile.isNullOrBlank() &&
            !storePassword.isNullOrBlank() &&
            !keyAlias.isNullOrBlank() &&
            !keyPassword.isNullOrBlank()
        ) {
            create("release") {
                this.storeFile = if (file(storeFile).exists()) file(storeFile) else file("../" + storeFile)
                this.storePassword = storePassword
                this.keyAlias = keyAlias
                this.keyPassword = keyPassword
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.findByName("release") // null → unsigned
        }
        debug {
            buildConfigField("String", "API_BASE_URL", "\"${project.findProperty("apiBaseUrlDev") ?: "https://ki-kompetenz-training.org"}\"")
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
        buildConfig = true
    }
    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.retrofit)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    implementation(libs.datastore.preferences)
    implementation(libs.security.crypto)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.sceneview)
    debugImplementation(libs.androidx.ui.tooling)
    testImplementation(libs.junit)
    testImplementation(libs.truth)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.androidx.core.testing)
    
    // Testing & Quality
    testImplementation(libs.kotest.runner)
    testImplementation(libs.kotest.assertions)
    testImplementation(libs.kotest.property)
    testImplementation(libs.okhttp.mockwebserver)
    
    // Moshi for API Contract Tests
    testImplementation(libs.moshi)
    testImplementation(libs.moshi.kotlin)
    testImplementation(libs.retrofit.moshi)
    
    // Android UI Testing
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.test.manifest)
}

tasks.withType<JacocoReport> {
    dependsOn("testDebugUnitTest")
    reports {
        xml.required = true
        html.required = true
    }
}