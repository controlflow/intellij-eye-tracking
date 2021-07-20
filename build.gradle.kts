import org.jetbrains.changelog.closure
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  // Java support
  id("java")
  // Kotlin support
  id("org.jetbrains.kotlin.jvm") version "1.4.20"
  // gradle-intellij-plugin - read more: https://github.com/JetBrains/gradle-intellij-plugin
  id("org.jetbrains.intellij") version "0.6.5"
  // gradle-changelog-plugin - read more: https://github.com/JetBrains/gradle-changelog-plugin
  id("org.jetbrains.changelog") version "0.6.2"
}

// Import variables from gradle.properties file
val pluginGroup: String by project
// `pluginName_` variable ends with `_` because of the collision with Kotlin magic getter in the `intellij` closure.
// Read more about the issue: https://github.com/JetBrains/intellij-platform-plugin-template/issues/29
val pluginName_: String by project
val pluginVersion: String by project
val pluginSinceBuild: String by project
val pluginUntilBuild: String by project
val pluginVerifierIdeVersions: String by project

val platformType: String by project
val platformVersion: String by project
val platformPlugins: String by project
val platformDownloadSources: String by project

group = pluginGroup
version = pluginVersion

repositories {
  mavenCentral()
  jcenter()
}

// Configure gradle-intellij-plugin plugin.
// Read more: https://github.com/JetBrains/gradle-intellij-plugin
intellij {
  pluginName = pluginName_
  version = platformVersion
  type = platformType
  downloadSources = platformDownloadSources.toBoolean()
  updateSinceUntilBuild = true

  setPlugins(*platformPlugins
    .split(',')
    .map(String::trim)
    .filter(String::isNotEmpty)
    .toTypedArray())
}

tasks {
  // Set the compatibility versions to 1.8
  withType<JavaCompile> {
    sourceCompatibility = "1.8"
    targetCompatibility = "1.8"
  }

  listOf("compileKotlin", "compileTestKotlin").forEach {
    getByName<KotlinCompile>(it) {
      kotlinOptions.jvmTarget = "11"
      kotlinOptions.apiVersion = "1.3"
    }
  }

  patchPluginXml {
    version(pluginVersion)
    sinceBuild(pluginSinceBuild)
    untilBuild(pluginUntilBuild)
    changeNotes(
      closure {
        changelog.getLatest().toHTML()
      }
    )
  }

  val addBinariesToSandbox by registering(Copy::class) {
    from ("$projectDir\\lib")
    into(file("$buildDir\\idea-sandbox\\plugins\\${rootProject.name}\\lib"))
    include("*.dll")
  }

  prepareSandbox {
    finalizedBy(addBinariesToSandbox)
  }

  runPluginVerifier {
    ideVersions(pluginVerifierIdeVersions)
  }

  publishPlugin {
    dependsOn("patchChangelog")
    token(System.getenv("PUBLISH_TOKEN"))

    // pluginVersion is based on the SemVer (https://semver.org) and supports pre-release labels, like 2.1.7-alpha.3
    // Specify pre-release label to publish the plugin in a custom Release Channel automatically. Read more:
    // https://jetbrains.org/intellij/sdk/docs/tutorials/build_system/deployment.html#specifying-a-release-channel
    channels(pluginVersion
      .split('-')
      .getOrElse(1) { "default" }
      .split('.')
      .first())
  }
}