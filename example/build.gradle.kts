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
    helmVersion = "3.19.4"
}

testing {
    suites {
        named<JvmTestSuite>("test") {
            useJUnitJupiter()
            dependencies {
                implementation("com.rrmoore:helm-test-java:0.3.0-SNAPSHOT")
            }
            targets {
                all {
                    testTask {
                        systemProperty("helm.chart.path", layout.projectDirectory.dir("src/main/helm/gym-register").asFile.path)
                    }
                }
            }
        }
    }
}
