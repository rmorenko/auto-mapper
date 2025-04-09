plugins {
    kotlin("jvm") version "2.1.10"
    id("io.gitlab.arturbosch.detekt") version "1.23.8"
    id("org.jetbrains.dokka") version "1.8.10" // Add Dokka plugin for KDoc generation
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.10"
    jacoco
    `java-library`
}

group = "com.morenko.automapper"
version = "1.0-SNAPSHOT"

repositories {
    google()
    mavenCentral()
    maven("https://maven.tryformation.com/releases") {
        content {
            includeGroup("com.jillesvangurp")
        }
    }
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.squareup:kotlinpoet:1.13.0")
    implementation("com.squareup:kotlinpoet-ksp:2.1.0")
    implementation("com.google.devtools.ksp:symbol-processing-api:2.1.10-1.0.31")
    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:2.1.10")
    testImplementation("io.kotest:kotest-runner-junit5-jvm:5.9.1")
    testImplementation("io.kotest:kotest-property:5.9.1")
    testImplementation("io.kotest:kotest-extensions-junitxml:5.9.1")
    testImplementation("com.github.tschuchortdev:kotlin-compile-testing-ksp:1.6.0")
    testImplementation("com.google.devtools.ksp:symbol-processing-api:2.1.10-1.0.31")
    testImplementation("io.mockk:mockk:1.13.17")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    reports {
        junitXml.required.set(true)
    }
}

tasks.named("check") {
    dependsOn("detektMain", "detektTest")
}

tasks.register<Jar>("fatJar") {
    archiveBaseName.set("auto-mapper-with-dependencies")
    archiveVersion.set("1.0")
    from(sourceSets.main.get().output)

    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) }
    })

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

// Configure Dokka for KDoc generation
tasks.dokkaHtml.configure {
    outputDirectory.set(layout.buildDirectory.get().asFile.resolve("dokka"))
    dokkaSourceSets {
        configureEach {
            includes.from("module.md")
        }
    }
}

jacoco {
    toolVersion = "0.8.12"
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport) // report is always generated after tests run
}
tasks.jacocoTestReport {
    dependsOn(tasks.test) // tests are required to run before generating the report
    reports {
        xml.required = false
        csv.required = false
        html.outputLocation = layout.buildDirectory.dir("jacocoHtml")
    }
}
