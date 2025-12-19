@file:Suppress("UnstableApiUsage")


plugins {
    java
    `jvm-test-suite`
    id("com.rrmoore.gradle.helm-test-java")
    id("com.vanniktech.maven.publish") version "0.35.0"
}

repositories {
    mavenCentral()
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
        }
    }
}

helmToolchain {
    helmVersion = "3.19.4"
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

    coordinates("com.rrmoore", "helm-test-java", "0.1.0-SNAPSHOT")
    pom {
        name.set("Helm Test Java")
        description.set("A library for writing automated tests for Helm charts")
        inceptionYear.set("2025")
        url.set("https://github.com/robmoore-i/helm-test-java")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                name.set("Rob Moore")
                url.set("https://github.com/robmoore-i")
                email.set("robmoore121@gmail.com")
            }
        }
    }
}
