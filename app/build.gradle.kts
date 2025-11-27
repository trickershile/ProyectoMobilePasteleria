plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.example.ejemploprueba"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.example.ejemploprueba"
        minSdk = 24
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        buildConfig = true
    }

    flavorDimensions += listOf("env")
    productFlavors {
        create("devEmu") {
            dimension = "env"
            buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:8080/\"")
        }
        create("devDevice") {
            dimension = "env"
            buildConfigField("String", "BASE_URL", "\"http://192.168.1.100:8080/\"")
        }
        create("stage") {
            dimension = "env"
            buildConfigField("String", "BASE_URL", "\"https://stage.api.gustitosabroson.com/\"")
        }
        create("prod") {
            dimension = "env"
            buildConfigField("String", "BASE_URL", "\"https://api.gustitosabroson.com/\"")
        }
    }

    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
        }
        getByName("release") {
            isMinifyEnabled = false
        }
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    // Core
    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    // Lifecycle & ViewModel
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.activity:activity-ktx:1.7.2")

    // Glide
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Room
    implementation("androidx.room:room-runtime:2.5.2")
    implementation("androidx.room:room-ktx:2.5.2")
    ksp("androidx.room:room-compiler:2.5.2")

    // DrawerLayout
    implementation("androidx.drawerlayout:drawerlayout:1.1.1")

    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
    testImplementation("org.robolectric:robolectric:4.10.3")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.facebook.shimmer:shimmer:0.5.0")
}

tasks.register("quickDevEmu") {
    dependsOn("assembleDevEmuDebug", "lintDevEmuDebug")
}

tasks.register("quickDevDevice") {
    dependsOn("assembleDevDeviceDebug", "lintDevDeviceDebug")
}
