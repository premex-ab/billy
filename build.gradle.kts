import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import io.gitlab.arturbosch.detekt.Detekt

// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {

    repositories {
        maven {
            // until broken gradle plugin portal is fixed
            url = uri("https://premex.jfrog.io/artifactory/local-gradle-plugins/")
        }
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.12.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.22")
        classpath("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:1.23.8")
        classpath("com.google.gms:google-services:4.4.2")
        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

plugins {
    id("com.github.ben-manes.versions") version "0.51.0"
    id("io.gitlab.arturbosch.detekt") version "1.23.8"
    id("io.github.gradle-nexus.publish-plugin") version "2.0.0"
    id("org.jetbrains.kotlinx.binary-compatibility-validator") version "0.14.0"
    id("com.gladed.androidgitversion") version "0.4.14"
}

apiValidation {
    ignoredProjects.add("app")
}

androidGitVersion {
    tagPattern = "^v[0-9]+.*"
}

val gitOrLocalVersion: String =
    com.android.build.gradle.internal.cxx.configure.gradleLocalProperties(rootDir)
        .getProperty("VERSION_NAME", androidGitVersion.name().replace("v", ""))

version = gitOrLocalVersion
group = "se.warting.billy"

allprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")

    dependencies {
        detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.8")
    }

    detekt {
        autoCorrect = true
    }

    // https://github.com/otormaigh/playground-android/issues/27
    repositories {
        google()
        mavenCentral()
    }
}

detekt {
    autoCorrect = true
    buildUponDefaultConfig = true
    config.setFrom(files("$projectDir/config/detekt/detekt.yml"))
    baseline = file("$projectDir/config/detekt/baseline.xml")
}

tasks.withType<Detekt>().configureEach {
    reports {
        html.required.set(true)
        xml.required.set(false)
        txt.required.set(false)
        sarif.required.set(false)
        md.required.set(false)
    }
}

fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.toUpperCase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return isStable.not()
}

tasks.named("dependencyUpdates", DependencyUpdatesTask::class.java).configure {
    rejectVersionIf {
        isNonStable(candidate.version) && !isNonStable(currentVersion)
    }
}
