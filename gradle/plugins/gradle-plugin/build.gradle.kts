plugins {
    `java-gradle-plugin`
    id("org.jetbrains.kotlin.jvm") version "2.2.0"
    id("com.gradle.plugin-publish") version "2.0.0"
    signing
}

version = "1.1"
group = "com.rrmoore.gradle"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

gradlePlugin {
    website.set("https://github.com/robmoore-i/helm-test-java")
    vcsUrl.set("https://github.com/robmoore-i/helm-test-java")
    plugins {
        create("helmTestJavaPlugin") {
            id = "com.rrmoore.gradle.helm-test-java"
            implementationClass = "com.rrmoore.gradle.helm.test.HelmTestJavaPlugin"
            displayName = "Helm Test plugin"
            description = "Companion Gradle plugin for the helm-test-java library. When applied, this plugin downloads and unpacks a `helm` executable for use in your build."
            tags.set(listOf("helm", "chart", "kubernetes", "test", "tests", "testing"))
        }
    }
}

signing {
    val signingKeyId: String? by project
    val signingKey: String? by project
    val signingPassword: String? by project
    useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword)
}
