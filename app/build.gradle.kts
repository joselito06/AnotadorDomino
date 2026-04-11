plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.devtools.ksp)
    alias(libs.plugins.google.dagger.hilt)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.google.firebase.crashlytics)
}

android {
    namespace = "com.jbncode.anotadordomino"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.jbncode.anotadordomino"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"

        resValue("string", "app_version_name", "\"${versionName}\"")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
            isDebuggable = true

            manifestPlaceholders["admob_app_id"] = "ca-app-pub-3940256099942544~3347511713"
            resValue("string", "banner_ad_id", "ca-app-pub-3940256099942544/6300978111")
            resValue("string", "interstitial_ad_id", "ca-app-pub-3940256099942544/1033173712")
            resValue("string", "app_loaded_ad_id", "ca-app-pub-3940256099942544/9257395921")
            resValue("string", "native_ad_id", "ca-app-pub-3940256099942544/9257395921")
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            manifestPlaceholders["admob_app_id"] = "ca-app-pub-7091067576578095~2682004896"
            resValue("string", "banner_ad_id", "ca-app-pub-7091067576578095/7742759889")
            resValue("string", "interstitial_ad_id", "ca-app-pub-7091067576578095/9497593639")
            resValue("string", "app_loaded_ad_id", "ca-app-pub-7091067576578095/6871430292")
            resValue("string", "native_ad_id", "ca-app-pub-7091067576578095/7607028518")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    // Implementación de Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.appcompat)
    ksp(libs.androidx.room.compiler)

    // Implementación de Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Implementación de AdMob
    implementation(libs.play.services.ads)

    implementation(libs.androidx.compose.material.icons.extended)

    implementation(libs.datastore.preferences)

    implementation(libs.io.coil.compose)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.config)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}