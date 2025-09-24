plugins {
    id("com.android.application")
    id("com.google.gms.google-services") // Google Services plugin for Firebase
}

android {
    namespace = "com.example.WomenSafty"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.WomenSafty"
        minSdk = 25
        targetSdk = 34
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
    implementation("com.google.android.material:material:1.12.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.10.0")

    implementation("com.google.ai.client.generativeai:generativeai:0.2.2")
// Keep your existing Retrofit dependencies or remove them if not needed
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.google.ai.client.generativeai:generativeai:0.3.0")
}
