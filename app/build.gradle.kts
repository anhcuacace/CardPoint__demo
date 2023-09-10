@file:Suppress("UnstableApiUsage")

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
//    id("dagger.hilt.android.plugin")
    kotlin("kapt")
}

android {
    namespace = "tunanh.test_app"
    compileSdk = 33

    defaultConfig {
        applicationId = "tunanh.test_app"
        minSdk = 28
        targetSdk = 33
        versionCode = 2
        versionName = "card_demo"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
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
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.3"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

//    implementation(libs.kotlin.stdlib)
//    implementation(libs.kotlinx.coroutines.android)
//    implementation(libs.google.material)
    implementation(libs.app.compat)
    implementation(libs.core.ktx)
    implementation(libs.lifecycle.runtime.ktx)
//    implementation(libs.androidx.emoji2.views.helper)
    implementation(libs.activity.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)
    implementation(libs.material3)
    implementation(libs.material)
    implementation(libs.material.icon)
    implementation(libs.datastore.preferences)
//    implementation(files("/libs/boltsdk_3.0.86.aar"))
    implementation(files("libs/database-connection-1.00.jar"))
    implementation(files("libs/Universal_SDK_1.00.164_os.jar"))
//    implementation(files("libs/boltsdk_3.0.86.aar"))
    implementation(libs.accompanist.permissions)
    implementation(libs.androidx.navigation.common.ktx)
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.coroutines.core)
//    implementation(libs.coroutines.flow)
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp3)
    implementation(libs.logging.interceptor)
    implementation(libs.gson)
    implementation(libs.timber)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.ui.test.junit4)

    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)

//    implementation(libs.androidx.hilt.navigation.compose)
//    implementation(libs.hilt.android)
//    kapt(libs.hilt.compiler)
//    kapt(libs.hilt.ext.compiler)
}