plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.ksp)
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
    implementation(libs.bundles.ksp)
    implementation(project(":auto-mapper-processor"))
    ksp(project(":auto-mapper-processor"))
}

tasks.dokkaHtml.configure {
    outputDirectory.set(layout.buildDirectory.get().asFile.resolve("dokka"))
    dokkaSourceSets {
        configureEach {
            includes.from("module.md")
        }
    }
}

tasks.jacocoTestReport {
    dependsOn(tasks.test) // tests are required to run before generating the report
    reports {
        xml.required = false
        csv.required =  true
        html.outputLocation = layout.buildDirectory.dir("jacocoHtml")
    }
}

jacoco {
    toolVersion = "0.8.12"
}

