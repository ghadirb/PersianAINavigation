plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    // Temporarily disabled until google-services.json is configured
    // id("com.google.gms.google-services")
}

android {
    namespace = "ir.navigation.persian.ai"
    compileSdk = 34

    defaultConfig {
        applicationId = "ir.navigation.persian.ai"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        ndk {
            abiFilters.addAll(listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64"))
        }
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    kotlinOptions {
        jvmTarget = "17"
    }
    
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += listOf(
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/license.txt",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt",
                "META-INF/notice.txt",
                "META-INF/*.kotlin_module"
            )
        }
    }
}

dependencies {
    // AndroidX Core
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    
    // MapLibre GL for map display only
    implementation("org.maplibre.gl:android-sdk:11.0.0")
    // Using OSRM API via Retrofit for routing (no SDK conflicts)
    
    // GraphHopper Core for routing (Temporarily commented for build)
    // TODO: Add GraphHopper when needed or use alternative routing
    // implementation("com.graphhopper:graphhopper-core:5.3")
    // implementation("com.graphhopper:graphhopper-reader-osm:5.3")
    
    // Location Services
    implementation("com.google.android.gms:play-services-location:21.1.0")
    
    // Networking
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    
    // Google Drive API (Temporarily disabled - will use direct HTTP instead)
    // implementation("com.google.android.gms:play-services-auth:20.7.0")
    // implementation("com.google.apis:google-api-services-drive:v3-rev20220815-2.0.0")
    // implementation("com.google.api-client:google-api-client-android:2.2.0")
    // implementation("com.google.http-client:google-http-client-gson:1.43.3")
    
    // ML/AI - TensorFlow Lite & ONNX Runtime
    implementation("org.tensorflow:tensorflow-lite:2.14.0")
    implementation("org.tensorflow:tensorflow-lite-support:0.4.4")
    implementation("com.microsoft.onnxruntime:onnxruntime-android:1.17.0")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")
    
    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    
    // Room Database for local storage
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    
    // JSON
    implementation("com.google.code.gson:gson:2.10.1")
    
    // Preferences DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    
    // Work Manager for background tasks
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    
    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
