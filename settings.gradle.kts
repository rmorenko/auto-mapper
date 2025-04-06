plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "auto-mapper"
include(":auto-mapper-processor")
include(":auto-mapper-workaround")
