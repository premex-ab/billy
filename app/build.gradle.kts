import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    id("com.android.application")
    id("kotlin-android")
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
}


val customApplicationId: String =
    gradleLocalProperties(rootDir, providers).getProperty("APPLICATION_ID", "se.warting.sample")

android {
    compileSdk = 36

    defaultConfig {
        applicationId = customApplicationId
        minSdk = 23
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    buildFeatures {
        dataBinding = false
        compose = true
    }

    lint {
        baseline = file("lint-baseline.xml")
        checkReleaseBuilds = true
        checkAllWarnings = true
        warningsAsErrors = true
        abortOnError = true
        disable.add("LintBaseline")
        disable.add("GradleDependency")
        disable.add("NewerVersionAvailable")
        checkDependencies = true
        checkGeneratedSources = false
        sarifOutput = file("../lint-results-lib.sarif")
    }
    namespace = "se.warting.sampleapp"
}

kotlin {
    jvmToolchain(21)
}

dependencies {

    val composeBom = platform("androidx.compose:compose-bom:2026.03.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)
    implementation(project(":flow"))

    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")

    implementation("androidx.core:core-ktx:1.18.0")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.13.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material:material")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.4")
    implementation("androidx.activity:activity-compose:1.13.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
}