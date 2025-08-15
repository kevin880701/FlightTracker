plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.dagger.hilt.android)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.lhr.flighttracker"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.lhr.flighttracker"
        minSdk = 24
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    // --- 核心 & Lifecycle KTX ---
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.work.runtime.ktx)
    // ViewModel for Compose
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    // Collect Flow with lifecycle awareness in Compose
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.accompanist.permissions)

    // --- Jetpack Compose ---
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    // Compose Navigation for screen routing
    implementation(libs.androidx.navigation.compose)

    // --- 依賴注入 (Hilt) ---
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    // Hilt integration for Compose Navigation
    implementation(libs.hilt.navigation.compose)

    // --- 網路 (Retrofit & OkHttp) ---
    implementation(platform(libs.okhttp.bom))
    implementation(libs.retrofit)
    implementation(libs.converter.moshi) // Retrofit's Moshi converter
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor) // For logging network requests

    // --- JSON 解析 (Moshi) ---
    implementation(libs.moshi.kotlin)
    ksp(libs.moshi.kotlin.codegen)

    // --- 圖片載入 (Coil) ---
    implementation(libs.coil.compose)

    // --- 非同步 (Coroutines) ---
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // --- 單元測試 (Unit Tests) ---
    testImplementation(libs.junit)
    testImplementation(libs.mockk.android)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine) // For testing Kotlin Flow

    // --- UI/整合測試 (Android Tests) ---
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    // Hilt testing support
    androidTestImplementation(libs.hilt.android.testing)
//    kaptAndroidTest(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // --- Debug ---
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // --- 資料庫 (Room) ---
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

}

hilt {
    enableAggregatingTask = false
}