plugins {
    id("com.android.application")
    id("kotlin-parcelize")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "tool.xfy9326.milink.nfc"
    compileSdk = 35

    defaultConfig {
        applicationId = "tool.xfy9326.milink.nfc"
        minSdk = 29
        targetSdk = 35
        versionCode = 22
        versionName = "2.0.2"

        @Suppress("UnstableApiUsage")
        androidResources.localeFilters += "zh"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            manifestPlaceholders["ApplicationId"] =
                defaultConfig.applicationId + applicationIdSuffix
            resValue("string", "app_name", "MiLink NFC Debug")

            versionNameSuffix = "-debug"
        }
        register("beta") {
            initWith(getByName("release"))
            matchingFallbacks += "release"

            applicationIdSuffix = ".beta"
            manifestPlaceholders["ApplicationId"] =
                defaultConfig.applicationId + applicationIdSuffix
            resValue("string", "app_name", "MiLink NFC Beta")

            versionNameSuffix = "-beta"

            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            packaging {
                resources.excludes += "DebugProbesKt.bin"
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
        freeCompilerArgs += "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api"
    }

    packaging {
        resources {
            excludes += listOf(
                "META-INF/{AL2.0,LGPL2.1}",
                "META-INF/*.version",
                "META-INF/CHANGES",
                "META-INF/README.md",
                "META-INF/DEPENDENCIES",
                "META-INF/{LICENSE,LICENSE.txt,license.txt}",
                "META-INF/{NOTICE,NOTICE.txt,notice.txt}",
                "META-INF/INDEX.LIST",
                "okhttp3/internal/publicsuffix/NOTICE",
                "**/*.proto",
                "kotlin-tooling-metadata.json"
            )
        }
    }

    lint {
        checkReleaseBuilds = false
    }
}

dependencies {
    implementation(project(":XiaomiNFCProtocol"))

    // Kotlin
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.1")

    // AndroidX Core
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.core:core-splashscreen:1.0.1")

    // AndroidX
    implementation("androidx.datastore:datastore-preferences:1.1.2")
    implementation("androidx.activity:activity-compose:1.10.0")

    // Compose BOM
    val composeBom = platform("androidx.compose:compose-bom:2025.01.00")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.animation:animation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    // Compose Accompanist
    val accompanist = "0.37.0"
    implementation("com.google.accompanist:accompanist-drawablepainter:$accompanist")

    // AndroidX Compose Lifecycle
    val lifecycleVersion = "2.8.7"
    implementation("androidx.lifecycle:lifecycle-runtime-compose:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycleVersion")

    val navVersion = "2.8.5"
    implementation("androidx.navigation:navigation-compose:$navVersion")

    testImplementation("junit:junit:4.13.2")

    androidTestImplementation(composeBom)
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")

    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}