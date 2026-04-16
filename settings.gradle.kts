rootProject.name = "helm-test-java"

pluginManagement {
    includeBuild("gradle/plugins")
}

plugins {
    id("com.gradle.develocity") version "4.4.0"
}

develocity {
    server = "https://develocity.grdev.net"
}

include(":library")
include(":example")
