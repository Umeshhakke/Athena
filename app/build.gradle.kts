plugins {
    id("com.android.application")
    id("com.google.gms.google-services") // Google Services plugin for Firebase
}

android {
    namespace = "com.example.WomenSafty"
    compileSdk = 35  // Updated from 34 to 35

    defaultConfig {
        applicationId = "com.example.WomenSafty"
        minSdk = 25
        targetSdk = 34  // You can keep this as 34 or update to 35 later
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        viewBinding = true // Optional: enables view binding if needed
    }
}

dependencies {
    // Firebase BOM - keeps versions in sync
    implementation(platform("com.google.firebase:firebase-bom:34.3.0"))

    // Firebase libraries
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")       // for authentication
    implementation("com.google.firebase:firebase-firestore")   // Firestore database
    implementation("com.google.firebase:firebase-database")    // Realtime database if needed

    // AndroidX & Material Design
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation(libs.google.material)

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.10.0")

    implementation("com.google.ai.client.generativeai:generativeai:0.2.2")
    implementation("com.google.ai.client.generativeai:generativeai:0.3.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("com.google.android.gms:play-services-maps:18.2.0")

    // CameraX - Updated to latest version compatible with compileSdk 35
    val camerax_version = "1.5.0"  // Latest version for API 35
    implementation("androidx.camera:camera-core:${camerax_version}")
    implementation("androidx.camera:camera-camera2:${camerax_version}")
    implementation("androidx.camera:camera-lifecycle:${camerax_version}")
    implementation("androidx.camera:camera-video:${camerax_version}")
    implementation("androidx.camera:camera-view:${camerax_version}")
    implementation("androidx.camera:camera-extensions:${camerax_version}")
}