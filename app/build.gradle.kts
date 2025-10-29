plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)   // keep kapt for Hilt
    alias(libs.plugins.ksp)           // use KSP for Moshi codegen
    alias(libs.plugins.hilt)
}

android {
    namespace = "id.co.app.pocpenalty"
    compileSdk = 35

    defaultConfig {
        applicationId = "id.co.app.pocpenalty"
        minSdk = 24
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }
}

dependencies {
    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.material3)
    debugImplementation(libs.androidx.ui.tooling)
    implementation("androidx.compose.material:material-icons-extended")

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation("androidx.hilt:hilt-navigation-compose:${libs.versions.hiltNavigationCompose.get()}")

    // Coroutines
    implementation(libs.bundles.coroutines)

    // Networking
    implementation(libs.bundles.retrofit)

    // JSON (pick one or both)
    implementation(libs.gson)                 // if you still use Gson somewhere
    implementation(libs.multidex)                 // if you still use Gson somewhere
    implementation(libs.moshi.kotlin)         // Moshi runtime
    ksp(libs.moshi.codegen)                   // Moshi codegen via KSP (NOT kapt)

    // Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)

    // CameraX
    implementation(libs.camera.core)
    implementation(libs.camera.camera2)
    implementation(libs.camera.lifecycle)
    implementation(libs.camera.view)

    testImplementation(libs.junit)
    // instrumented tests
    androidTestImplementation(libs.androidx.junit)           // androidx.test.ext:junit
    androidTestImplementation(libs.androidx.espresso.core)   // Espresso
}

kapt {
    correctErrorTypes = true
}