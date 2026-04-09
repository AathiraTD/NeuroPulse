plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services)
}

// Only apply google-services if the JSON exists, allowing "Quick Start" mode.
if (file("google-services.json").exists()) {
    apply(plugin = "com.google.gms.google-services")
}

android {
    namespace = "com.neuropulse"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.neuropulse"
        minSdk = 26          // Health Connect minimum
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0-poc"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Secrets injected from local.properties — never hardcode API keys here.
        // See docs/adr/... and README.md for local setup instructions.
        buildConfigField(
            "String",
            "GEMINI_API_KEY",
            "\"${project.findProperty("GEMINI_API_KEY") ?: ""}\""
        )
        buildConfigField(
            "String",
            "FIREBASE_WEB_CLIENT_ID",
            "\"${project.findProperty("FIREBASE_WEB_CLIENT_ID") ?: ""}\""
        )
    }

    buildTypes {
        debug {
            // Allows bypassing Firebase Auth on the emulator during development.
            // Never set true in release — guarded by BuildConfig.SKIP_AUTH check in NavGraph.
            buildConfigField("Boolean", "SKIP_AUTH", "false")
            isDebuggable = true
        }
        release {
            buildConfigField("Boolean", "SKIP_AUTH", "false")
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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
}

dependencies {
    // AndroidX core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    // Material Components (required for Material3 XML themes)
    implementation(libs.google.android.material)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.firebase.auth)
    implementation(libs.androidx.credentials.play.services.auth)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(libs.hilt.navigation.compose)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // DataStore
    implementation(libs.datastore.preferences)

    // WorkManager
    implementation(libs.workmanager.ktx)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Health Connect
    implementation(libs.health.connect)

    // Google Identity (One-Tap & CredentialManager)
    implementation(libs.credentials)
    implementation(libs.google.id)

    // Biometric (fingerprint / face unlock — DD-010)
    implementation(libs.biometric)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.firestore.ktx)

    // Timber
    implementation(libs.timber)

    // LangChain4j + Gemini Flash (Phase 4 — declared now so Hilt modules compile)
    implementation(libs.langchain4j.core)
    implementation(libs.langchain4j.google.gemini)

    // Testing
    testImplementation(libs.junit5.api)
    testRuntimeOnly(libs.junit5.engine)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.android.compiler)
}
