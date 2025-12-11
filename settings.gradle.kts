plugins {
    id("com.gradle.develocity") version "4.3"
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

buildCache {
    local {
        isEnabled = true
    }
    remote<HttpBuildCache> {
        isEnabled = false
    }
}

develocity{
    buildScan {
        termsOfUseUrl.set("https://gradle.com/help/legal-terms-of-use")
        termsOfUseAgree.set("yes")
        publishing.onlyIf { false }
    }
}

// Re-enable once dependabot works  // https://github.com/otormaigh/playground-android/issues/27
//dependencyResolutionManagement {
//    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
//    repositories {
//        google()
//        mavenCentral()
//        jcenter() // Warning: this repository is going to shut down soon
//    }
//}
rootProject.name = "Billing flow"
include(":app")
include(":flow")
