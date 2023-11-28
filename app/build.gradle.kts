import com.google.protobuf.gradle.id

plugins {
    id("com.android.application")
    id("kotlin-parcelize")
    id("org.jetbrains.kotlin.android")
    id("com.google.protobuf")
}

android {
    namespace = "tool.xfy9326.milink.nfc"
    compileSdk = 34

    defaultConfig {
        applicationId = "tool.xfy9326.milink.nfc"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        resourceConfigurations += "zh"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            manifestPlaceholders["ApplicationId"] = defaultConfig.applicationId + applicationIdSuffix
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")

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
        freeCompilerArgs += "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api,androidx.compose.foundation.layout.ExperimentalLayoutApi"
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
                "google/protobuf/*.proto",
                "kotlin-tooling-metadata.json"
            )
        }
    }
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.25.1"
    }

    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                id("java") {
                    option("lite")
                }
            }
        }
    }
}

dependencies {
    // KotlinX Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Protocol Buffer
    implementation("com.google.protobuf:protobuf-javalite:3.25.1")

    // AndroidX Core
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.core:core-splashscreen:1.0.1")

    // AndroidX
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.datastore:datastore:1.0.0")
    implementation("androidx.activity:activity-compose:1.8.1")

    // Compose BOM
    val composeBom = platform("androidx.compose:compose-bom:2023.10.01")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    // AndroidX Compose Lifecycle
    val lifecycleVersion = "2.6.2"
    implementation("androidx.lifecycle:lifecycle-runtime-compose:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycleVersion")

    testImplementation("junit:junit:4.13.2")

    androidTestImplementation(composeBom)
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")

    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}