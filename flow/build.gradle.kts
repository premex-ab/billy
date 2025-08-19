
plugins {
    id("com.android.library")
    id("kotlin-android")
    id("org.jetbrains.dokka") version "2.0.0"
    id("com.vanniktech.maven.publish") version "0.34.0"
}



mavenPublishing {

    publishToMavenCentral()
    signAllPublications()

    pom {
        name.set("Billy")
        description.set("Billing flow")
        inceptionYear.set("2021")
        url.set("https://github.com/premex-ab/billy")
        licenses {
            license {
                name.set("MIT License")
                url.set("https://opensource.org/licenses/MIT")
                distribution.set("https://opensource.org/licenses/MIT")
            }
        }
        developers {
            developer {
                id.set("warting")
                name.set("Stefan Wärting")
                url.set("https://github.com/warting/")
            }
        }
        scm {
            url.set("https://github.com/premex-ab/billy")
            connection.set("scm:git:git://github.com/premex-ab/billy.git")
            developerConnection.set("scm:git:ssh://git@github.com/premex-ab/billy.git")
        }
    }
}

val PUBLISH_GROUP_ID: String by extra(rootProject.group as String)
val PUBLISH_VERSION: String by extra(rootProject.version as String)
val PUBLISH_ARTIFACT_ID by extra("flow")

group = PUBLISH_GROUP_ID
version = PUBLISH_VERSION

android {
    compileSdk = 34

    defaultConfig {
        minSdk = 21
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        viewBinding = false
        compose = false
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
        freeCompilerArgs = listOfNotNull(
            "-Xopt-in=kotlin.RequiresOptIn",
            "-Xskip-prerelease-check"
        )
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
        sarifOutput = file("../lint-results-app.sarif")
    }

    publishing {
        multipleVariants {
            allVariants()
            withSourcesJar()
            withJavadocJar()
        }
    }

    namespace = "se.warting.billy.flow"
}

kotlin {
    jvmToolchain(21)
    // https://kotlinlang.org/docs/whatsnew14.html#explicit-api-mode-for-library-authors
    explicitApi()
}


dependencies {

    api("androidx.startup:startup-runtime:1.2.0")

    val billingVersion = "7.0.0"
    api("com.android.billingclient:billing-ktx:$billingVersion")

    implementation("androidx.annotation:annotation:1.9.1")

    val lifecycle_version = "2.7.0"

    implementation("androidx.lifecycle:lifecycle-process:$lifecycle_version")

    // val composeVersion = "1.0.3"
    // implementation("androidx.compose.runtime:runtime:$composeVersion")
    // implementation("androidx.compose.ui:ui:$composeVersion")

    // val coroutineVersion = "1.5.2"
    // implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:$coroutineVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")

    // implementation("androidx.core:core-ktx:1.6.10")
    // implementation("androidx.appcompat:appcompat:1.3.1")
    // implementation("com.google.android.material:material:1.4.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
}