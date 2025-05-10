import org.jetbrains.intellij.platform.gradle.TestFrameworkType

val propertyPluginGroup = providers.gradleProperty("pluginGroup").get()
val propertyPluginVersion = providers.gradleProperty("pluginVersion").get()
val propertyPlatformVersion = providers.gradleProperty("platformVersion").get()
val propertyPluginSinceBuild = providers.gradleProperty("pluginSinceBuild").get()
val propertyBundledPlugins = providers.gradleProperty("platformBundledPlugins").map { it.split(",") }.get()

plugins {
    id("java")
    id("org.jetbrains.intellij.platform") version "2.5.0"
}

group = propertyPluginGroup
version = propertyPluginVersion

repositories {
    mavenCentral()

    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        intellijIdeaUltimate(propertyPlatformVersion)
        bundledPlugins(propertyBundledPlugins)
        testFramework(TestFrameworkType.Platform)
    }

    testImplementation("junit:junit:4.13.2")
    testRuntimeOnly("junit:junit:4.13.2")
}

intellijPlatform {
    pluginConfiguration {
        version = propertyPluginVersion

        ideaVersion {
            sinceBuild = propertyPluginSinceBuild
        }
    }
    pluginVerification {
        ides {
            recommended()
        }
    }
}