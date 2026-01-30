@file:Suppress("UnstableApiUsage")

import java.net.URI

plugins {
    java
    `jvm-test-suite`
    id("com.rrmoore.gradle.helm-test-java")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()

    maven {
        name = "Central Portal Snapshots"
        url = URI.create("https://central.sonatype.com/repository/maven-snapshots")
        content {
            includeModule("com.rrmoore", "helm-test-java")
        }
    }
}

helmToolchain {
    helmVersion = "4.0.4"
}

testing {
    suites {
        named<JvmTestSuite>("test") {
            useJUnitJupiter()
            dependencies {
                implementation("com.rrmoore:helm-test-java:1.1-SNAPSHOT")
                implementation("org.hamcrest:hamcrest:3.0")
            }
        }
    }
}
