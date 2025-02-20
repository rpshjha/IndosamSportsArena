plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android") // Update Kotlin version
}

android {
    namespace = "com.indosam.sportsarena"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.indosam.sportsarena"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true // Enable vector drawable support
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
        sourceCompatibility = JavaVersion.VERSION_17 // Update to Java 17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17" // Update to Java 17
    }

    buildFeatures {
        compose = true // Enable Jetpack Compose
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.0" // Match with your Kotlin version
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core dependencies
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")

    // Compose dependencies
    implementation("androidx.activity:activity-compose:1.7.2")
    implementation("androidx.compose.ui:ui:1.5.0")
    implementation("androidx.compose.ui:ui-tooling-preview:1.5.0")

    // Material Design Components (Add this line)
    implementation("com.google.android.material:material:1.9.0")

    // Material 3
    implementation("androidx.compose.material3:material3:1.1.1")
    implementation("androidx.compose.material:material-icons-extended:1.5.0")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.0")

    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")

    implementation("io.coil-kt:coil-compose:2.4.0")

    // Gson for JSON parsing
    implementation("com.google.code.gson:gson:2.10.1")



    // Testing dependencies
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.5.0")
    debugImplementation("androidx.compose.ui:ui-tooling:1.5.0")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.5.0")
}
