plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)  // required by Supabase DTOs
}

android {
    namespace = "com.example.final_project"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.final_project"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Task A Supabase project — committed so any teammate machine can run without extra setup.
        buildConfigField(
            "String",
            "SUPABASE_URL",
            "\"https://tumloddgamefuouqurww.supabase.co/\"",
        )
        buildConfigField(
            "String",
            "SUPABASE_ANON_KEY",
            "\"sb_publishable_7lgBlUxzcU4on4W1Kx1fwg_OABvIy5A\"",
        )
        buildConfigField("boolean", "USE_FAKE_SONGS", "false")
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
    buildFeatures {
        compose = true
        buildConfig = true   // for SUPABASE_URL / SUPABASE_KEY build-config fields
    }
}

dependencies {
    // ── AndroidX Core ────────────────────────────────────────────────────────
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.appcompat)

    // ── Jetpack Compose ───────────────────────────────────────────────────────
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)

    // ── DI — Koin ────────────────────────────────────────────────────────────
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)

    // ── Coroutines ────────────────────────────────────────────────────────────
    implementation(libs.kotlinx.coroutines.android)

    // ── Serialization ─────────────────────────────────────────────────────────
    implementation(libs.kotlinx.serialization.json)

    // ── Network — Ktor (required by Supabase SDK) ─────────────────────────────
    implementation(libs.ktor.client.android)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.serialization.kotlinx.json)

    // ── Supabase ──────────────────────────────────────────────────────────────
    implementation(platform(libs.supabase.bom))
    implementation(libs.supabase.postgrest)   // Database queries
    implementation(libs.supabase.realtime)    // WebSocket / Realtime (chat)
    implementation(libs.supabase.storage)     // File storage (audio + covers)
    implementation(libs.supabase.auth)        // Authentication
    implementation(libs.supabase.compose.auth)// Compose auth UI helpers

    // ── Tests ─────────────────────────────────────────────────────────────────
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}