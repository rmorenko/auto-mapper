plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.serialization)
    alias(libs.plugins.detekt)
    alias(libs.plugins.dokka)
    alias(libs.plugins.release)
    jacoco
    `maven-publish`
}

repositories {
    google()
    mavenCentral()
    maven("https://maven.tryformation.com/releases") {
        content {
            includeGroup("com.jillesvangurp")
        }
    }
}

group = "io.github.rmorenko"
version = properties["version"] ?: "UNKNOWN"

release {
    failOnCommitNeeded.set(true)
    failOnPublishNeeded.set(true)
    failOnSnapshotDependencies.set(true)
    failOnUnversionedFiles.set(true)
    failOnUpdateNeeded.set(true)
    preTagCommitMessage.set("[Gradle Release Plugin] - pre tag commit: ")
    tagCommitMessage.set("[Gradle Release Plugin] - creating tag: ")
    newVersionCommitMessage.set("[Gradle Release Plugin] - new version commit: ")
    tagTemplate.set("\${version}")
    versionPropertyFile.set("gradle.properties")
    snapshotSuffix.set("-SNAPSHOT")
    buildTasks.set(emptyList<String>())

    git {
        requireBranch.set("master")
    }
}
