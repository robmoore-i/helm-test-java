@file:Suppress("UnstableApiUsage")

import java.net.URI

plugins {
    java
    `jvm-test-suite`
}

repositories {
    maven {
        name = "Central Portal Snapshots"
        url = URI.create("https://central.sonatype.com/repository/maven-snapshots")
        content {
            includeModule("com.rrmoore", "helm-test-java")
        }
    }

    mavenCentral()
}

testing {
    suites {
        named<JvmTestSuite>("test") {
            useJUnitJupiter()
            dependencies {
                implementation("com.rrmoore:helm-test-java:0.1.0-SNAPSHOT")
            }
        }
    }
}
