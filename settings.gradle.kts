rootProject.name = "helm-test-java"

pluginManagement {
    includeBuild("gradle/plugins")
}

include(":library")
include(":example")
