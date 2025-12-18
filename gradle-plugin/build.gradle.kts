plugins {
    `java-gradle-plugin`
    id("com.gradle.plugin-publish") version "2.0.0"
}

version = "0.1.0"
group = "com.rrmoore.gradle"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

gradlePlugin {
    website.set("https://robmoore-i.github.io")
    vcsUrl.set("https://github.com/robmoore-i/helm-test-java")
    plugins {
        create("helmTestJavaPlugin") {
            id = "com.rrmoore.gradle.helm-test-java"
            implementationClass = "com.rrmoore.gradle.helm.test.HelmTestJavaPlugin"
            displayName = "Helm Test plugin"
            description = "Write automated tests for Helm charts."
            tags.set(listOf("helm", "chart", "kubernetes", "test", "tests", "testing"))
        }
    }
}
