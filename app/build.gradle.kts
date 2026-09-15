plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.0"
}

android {
    namespace = "com.example.xuimanager"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.xuimanager"
        minSdk = 26
        targetSdk = 35
        versionCode = 3
        versionName = "1.2.2"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
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
    }
}

dependencies {
    // Android Core & Lifecycle
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation("androidx.activity:activity-ktx:1.9.0")

    // Jetpack Compose UI & Navigation
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation("androidx.navigation:navigation-compose:2.8.0")
    implementation("androidx.compose.material:material-icons-extended:1.7.0")
    implementation("androidx.compose.material:material:1.7.0")

    // Сетевой слой (Retrofit + OkHttp)
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // SSH Подключение для автоустановки (mwiede jsch fork для поддержки современных алгоритмов SSH/RSA-SHA2/Ed25519)
    implementation("com.github.mwiede:jsch:2.28.7")

    // Биометрия (Fingerprint / Face Unlock)
    implementation("androidx.biometric:biometric:1.2.0-alpha05")

    // Фоновые задачи и Уведомления (Alerts)
    implementation("androidx.work:work-runtime-ktx:2.9.1")

    // Настройки приложения (DataStore)
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Генерация QR-кодов ключей
    implementation("com.google.zxing:core:3.5.3")
}