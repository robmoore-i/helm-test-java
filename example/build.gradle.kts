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

abstract class TestLifecycleService : BuildService<TestLifecycleService.Parameters>, AutoCloseable {
    interface Parameters : BuildServiceParameters
}

val testLifecycleService = gradle.sharedServices.registerIfAbsent("testLifecycleService", TestLifecycleService::class.java) {
    maxParallelUsages.set(1)
}

testing {
    suites {
        // Runs tests using the stable version of the library
        val test by existing(JvmTestSuite::class) {
            dependencies {
                implementation("com.rrmoore:helm-test-java:${properties["helm-test-java.library.version.stable"]}")
            }
        }

        withType<JvmTestSuite> {
            useJUnitJupiter()
            dependencies {
                implementation("org.hamcrest:hamcrest:3.0")
            }
            targets {
                all {
                    testTask {
                        usesService(testLifecycleService)
                        if (name != "test") {
                            sources.java.srcDir(test.map { it.sources.java.sourceDirectories })
                        }
                    }
                }
            }
        }

        // Runs tests using the local version of the library
        register<JvmTestSuite>("localVersionTest") {
            dependencies {
                implementation(project(":library"))
            }
        }

        // Runs tests using the snapshot version of the library
        register<JvmTestSuite>("snapshotVersionTest") {
            dependencies {
                // Intellij should automatically refresh this dependency when syncing or running the test task.
                // If it doesn't, you can manually refresh it by triggering a sync.
                implementation("com.rrmoore:helm-test-java:${properties["helm-test-java.library.version.development"]}-SNAPSHOT")
            }
        }
    }
}
