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

configurations.all {
    resolutionStrategy {
        cacheChangingModulesFor(0, TimeUnit.SECONDS)
    }
}

testing {
    suites {
        named<JvmTestSuite>("test") {
            useJUnitJupiter()
            dependencies {
                implementation("org.hamcrest:hamcrest:3.0")

                // Snapshot version
                // Note: You need to use the 'refresh dependencies' feature of Gradle/IDEA
                //       when updating the snapshot if you upload one during development.
                implementation("com.rrmoore:helm-test-java:1.2-SNAPSHOT")

                // Stable version
                // implementation("com.rrmoore:helm-test-java:1.1.1")
            }
        }
    }
}
