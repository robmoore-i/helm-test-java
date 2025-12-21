@file:Suppress("UnstableApiUsage")


plugins {
    java
    `java-library`
    `jvm-test-suite`
    id("com.rrmoore.gradle.helm-test-java")
    id("com.vanniktech.maven.publish") version "0.35.0"
}

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

dependencies {
    // Unfortunately there is no package that just exports the ability to parse the models,
    // so we drag in the whole Java client. Not the end of the world.
    api("io.kubernetes:client-java:25.0.0")
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
            dependencies {
                implementation("org.hamcrest:hamcrest:3.0")
            }
        }
    }
}

helmToolchain {
    helmVersion = "3.19.4"
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

    coordinates("com.rrmoore", "helm-test-java", "1.0")
    pom {
        name = "Helm Test Java"
        description = "A library for writing automated tests for Helm charts"
        inceptionYear = "2025"
        url = "https://github.com/robmoore-i/helm-test-java"
        licenses {
            license {
                name = "The Apache License, Version 2.0"
                url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                distribution = "https://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        }
        developers {
            developer {
                name = "Rob Moore"
                url = "https://github.com/robmoore-i"
                email = "robmoore121@gmail.com"
            }
        }
        scm {
            url = "https://github.com/robmoore-i/helm-test-java"
            connection = "scm:git:git://github.com/robmoore-i/helm-test-java.git"
            developerConnection = "scm:git:ssh://git@github.com/robmoore-i/helm-test-java.git"
        }
    }
}
