// Workaround for AGP bug when both ANDROID_PREFS_ROOT and ANDROID_USER_HOME env vars are set on host
try {
    val envClass = Class.forName("java.lang.ProcessEnvironment")
    val envField = envClass.getDeclaredField("theEnvironment")
    envField.isAccessible = true
    @Suppress("UNCHECKED_CAST")
    (envField.get(null) as? MutableMap<String, String>)?.remove("ANDROID_PREFS_ROOT")

    val ciEnvField = envClass.getDeclaredField("theCaseInsensitiveEnvironment")
    ciEnvField.isAccessible = true
    @Suppress("UNCHECKED_CAST")
    (ciEnvField.get(null) as? MutableMap<String, String>)?.remove("ANDROID_PREFS_ROOT")
} catch (_: Throwable) {
}

pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "SmartCampusClient"
include(":app")
