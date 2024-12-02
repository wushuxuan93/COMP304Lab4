import java.util.Properties
import java.io.FileInputStream

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlinx-serialization")
    id("com.google.devtools.ksp")
    id ("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}
//// Read the API key from local.properties
//val localProperties = Properties()
//val localPropertiesFile = rootProject.file("local.properties")
//if (localPropertiesFile.exists()) {
//    localProperties.load(FileInputStream(localPropertiesFile))
//}
//
//// Retrieve the DirectionsApiKey
//val directionsApiKey: String = localProperties.getProperty("DirectionsApiKey") ?: ""

android {
    namespace = "com.shuxuan.shuxuanwu_comp304lab4_ex1"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.shuxuan.shuxuanwu_comp304lab4_ex1"
        minSdk = 34
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // Inject the API key into BuildConfig
        buildConfigField("String", "DIRECTIONS_API_KEY", "\"AIZaSyA4EW_vZEAux0ZF1JUEbEcbbaIw_Q1JfV4\"")
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        buildConfig =  true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.2"
    }

    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
    }
}

dependencies {
    implementation(libs.core.ktx)
    implementation(libs.lifecycle)
    implementation(libs.activity.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.bundles.compose)
    implementation(libs.bundles.koin)
    implementation(libs.bundles.networking)
    implementation(libs.compose.navigation)
    implementation(libs.compose.window.size)
    implementation(libs.androidx.window)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    implementation(libs.work.runtime)
    implementation(libs.workmanager.koin)
    testImplementation(libs.test.junit)
    androidTestImplementation(libs.test.junitExt)
    androidTestImplementation(libs.test.espresso)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.test.compose.junit4)
    androidTestImplementation(libs.work.testing)
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.manifest)
    implementation(libs.play.services.location) // Location services
    implementation(libs.play.services.maps) // Google Maps SDK
    implementation(libs.maps.compose) // Jetpack Compose Maps library
    implementation(libs.coroutines.play.services)
    implementation (libs.kotlinx.coroutines.android.v160)  // Coroutines Android
    implementation (libs.kotlinx.coroutines.core.v160)
    implementation (libs.retrofit)
    implementation (libs.converter.gson)
    implementation (libs.compose.material3)
    implementation (libs.androidx.material.icons.extended)
    implementation (libs.maps.compose)
}

