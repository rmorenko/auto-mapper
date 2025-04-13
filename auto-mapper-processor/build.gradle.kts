plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.serialization)
    alias(libs.plugins.detekt)
    alias(libs.plugins.dokka)
    jacoco
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

dependencies {
    implementation(kotlin("stdlib"))
    implementation(libs.bundles.kotlinpoet)
    implementation(libs.bundles.ksp)
    testImplementation(kotlin("test"))
    testImplementation(libs.bundles.test)
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
    systemProperty("kotest.framework.classpath.scanning.autoscan.disable", "true")
    jvmArgs("-XX:+EnableDynamicAgentLoading", "-Djdk.instrument.traceUsage")
    finalizedBy(tasks.jacocoTestReport)
}
tasks.jacocoTestReport {
    dependsOn(tasks.test) // tests are required to run before generating the report
    reports {
        xml.required = false
        csv.required =  true
        html.outputLocation = layout.buildDirectory.dir("jacocoHtml")
    }
}
