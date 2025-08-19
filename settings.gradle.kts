plugins {
    id("com.gradle.enterprise") version "3.19.2"
}

buildCache {
    local {
        isEnabled = true
        directory = File(rootDir, "build-cache")
        removeUnusedEntriesAfterDays = 30
    }
    remote<HttpBuildCache> {
        isEnabled = false
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
