plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.serialization)
    alias(libs.plugins.detekt)
    alias(libs.plugins.dokka)
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

group = "com.morenko.automapper"
version = project.findProperty("version") ?: "SNAPSHOT"


