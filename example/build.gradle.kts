@file:Suppress("UnstableApiUsage")

import com.rrmoore.gradle.helm.test.PlatformIdentifier
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
    helmVersion = "3.19.4"
    platformIdentifier = PlatformIdentifier.DARWIN_AMD64
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
