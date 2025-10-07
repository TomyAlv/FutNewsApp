plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)

}

android {
    namespace = "com.example.espnapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.espnapp"
        minSdk = 26
        targetSdk = 35
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
    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures{
        viewBinding = true
    }

}

dependencies {

    implementation(libs.androidx.core.ktx)                         // Kotlin extensions for Android core APIs (convenience utilities, coroutines-friendly)
    implementation(libs.androidx.appcompat)                        // Backward-compatible Material/Android components on older Android versions
    implementation(libs.material)                                  // Google Material Components (Material 3 widgets, themes, typography)
    implementation(libs.androidx.activity)                         // Activity KTX (lifecycle-aware APIs, ViewModel integration, ActivityResult APIs)
    implementation(libs.androidx.constraintlayout)                 // ConstraintLayout for flexible, performant responsive UI layouts
    implementation(libs.androidx.media3.common.ktx)                // Media3 common + KTX helpers (media playback/data structures)

    testImplementation(libs.junit)                                 // Local unit testing with JUnit 4
    androidTestImplementation(libs.androidx.junit)                 // AndroidX JUnit extensions for instrumentation tests
    androidTestImplementation(libs.androidx.espresso.core)         // Espresso UI testing (view interactions and assertions)

    implementation("com.squareup.retrofit2:retrofit:2.11.0")       // Retrofit HTTP client for making type-safe REST calls
    implementation("com.squareup.retrofit2:converter-gson:2.11.0") // Gson converter: (de)serialize JSON to/from data classes

    implementation("com.squareup.picasso:picasso:2.8")             // Picasso image loading/caching into ImageViews

    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7") // Navigation Component for Fragments (KTX helpers)
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")  // Navigation UI bindings (BottomNav/Toolbar with NavController)

    implementation("com.github.bumptech.glide:glide:4.16.0")       // Glide image loading/caching with lifecycle awareness

    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0") // OkHttp interceptor to log HTTP request/response details

    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.6") // ViewModel KTX (coroutines, viewModelScope)
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.6")  // LiveData KTX (transformations, coroutine integration)

}

