@file:Suppress("UnstableApiUsage")

import java.net.URI

plugins {
    java
    `jvm-test-suite`
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

    exclusiveContent {
        forRepository {
            ivy {
                url = uri("https://get.helm.sh")
                patternLayout { artifact("[artifact]-v[revision]-[classifier].[ext]") }
                metadataSources {
                    artifact()
                }
            }
        }
        filter {
            includeGroup("io.github.helm")
        }
    }
}

val helmExecutable by configurations.creating {
    isCanBeResolved = true
    isCanBeConsumed = false
}

val helmPlatform = "darwin-amd64"
val helmVersion = "3.19.4"

dependencies {
    helmExecutable("io.github.helm:helm:$helmVersion:$helmPlatform@tar.gz")
}

val expandHelmExecutable by tasks.registering(Sync::class) {
    from(tarTree(helmExecutable.singleFile))
    into(layout.buildDirectory.dir("helm/executable/$helmPlatform-$helmVersion"))
}

class FileArgumentProvider(
    @Input val propertyName: String,
    @PathSensitive(PathSensitivity.NONE) @InputFile val inputFile: Provider<File>
) : CommandLineArgumentProvider {

    override fun asArguments(): Iterable<String?> {
        return listOf("-D$propertyName=${inputFile.get().path}")
    }
}

testing {
    suites {
        named<JvmTestSuite>("test") {
            useJUnitJupiter()
            dependencies {
                implementation("com.rrmoore:helm-test-java:0.1.0-SNAPSHOT")
            }
            targets {
                all {
                    testTask {
                        jvmArgumentProviders += FileArgumentProvider(
                            "com.rrmoore.helm.test.executable.path",
                            expandHelmExecutable.map { it.destinationDir.resolve("$helmPlatform/helm") }
                        )
                    }
                }
            }
        }
    }
}
