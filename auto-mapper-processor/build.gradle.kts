plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.serialization)
    alias(libs.plugins.detekt)
    alias(libs.plugins.dokka)

    jacoco
    id("java-library")
    id("maven-publish")
    id("signing")
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

group = rootProject.group
version = rootProject.version
description = "This project is designed to automatically generate mapping functions for Kotlin data classes."

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
    description = "Build jar with dependencies"
    group = "jars"
    archiveBaseName.set("${project.name}-with-dependencies")
    archiveVersion.set(version.toString())
    from(sourceSets.main.get().output)

    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) }
    })

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.dokkaHtml.configure {
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


val javadocJar by tasks.register<Jar>("javadocJar") {
    description = "Build jars with javadocs"
    group = "docs"
    archiveClassifier.set("javadoc")
    archiveBaseName.set(project.name)
    from(layout.buildDirectory.get().asFile.resolve("dokka/html"))
    mustRunAfter(tasks.named("dokkaHtml"))
}


java {
    withSourcesJar()
}


publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            artifact(javadocJar)
            artifact(tasks.named("fatJar").get()) {
                artifactId = project.name
                classifier = "jar-with-dependencies"
            }
            pom {
                name.set(project.name)
                description.set(project.description)
                url.set("https://github.com/rmorenko/auto-mapper")
                issueManagement {
                    url.set("https://github.com/rmorenko/auto-mapper/issues")
                }

                scm {
                    url.set("https://github.com/rmorenko/auto-mapper")
                    connection.set("scm:git://github.com/rmorenko/auto-mapper.git")
                    developerConnection.set("scm:git://github.com/rmorenko/auto-mapper.git")
                }

                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://www.mit.edu/~amini/LICENSE.md")
                        distribution.set("repo")
                    }
                }

                developers {
                    developer {
                        id.set("rmorenko")
                        name.set("Roman Morenko")
                        email.set("morenko83@gmail.com")
                        url.set("https://www.linkedin.com/in/roman-morenko-5091b6258/")
                    }
                }
            }
            repositories {
                maven {
                    name = "GitHubPackages"
                    url = uri("https://maven.pkg.github.com/rmorenko/auto-mapper")
                    credentials {
                        username = System.getenv("CENTRAL_USER_NAME")
                        password = System.getenv("MAVEN_CENTRAL_PASSWORD")
                    }
                }
                maven {
                    name = "MavenCentral"
                    url = uri("https://central.sonatype.com/api/v1/publisher")
                    credentials {
                        username = System.getenv("CENTRAL_USER_NAME")
                        password = System.getenv("MAVEN_CENTRAL_PASSWORD")
                    }
                }
            }
        }
    }

    signing {
        useInMemoryPgpKeys(
            System.getenv("GPG_SECRET_KEY"),
            System.getenv("GPG_PASSWORD")
        )
        sign(publishing.publications["maven"])
    }
}
