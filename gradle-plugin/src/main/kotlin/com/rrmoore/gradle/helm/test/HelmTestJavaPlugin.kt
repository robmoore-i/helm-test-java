package com.rrmoore.gradle.helm.test

import java.net.URI
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Sync
import org.gradle.api.tasks.testing.Test

class HelmTestJavaPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val extension = project.extensions.create("helmToolchain", HelmToolchainExtension::class.java)

        project.repositories.exclusiveContent { exclusiveContentRepository ->
            exclusiveContentRepository.forRepository {
                project.repositories.ivy { ivyArtifactRepository ->
                    ivyArtifactRepository.url = URI.create("https://get.helm.sh")
                    ivyArtifactRepository.patternLayout {
                        it.artifact("[artifact]-v[revision]-[classifier].[ext]")
                    }
                    ivyArtifactRepository.metadataSources {
                        it.artifact()
                    }
                }
            }
            exclusiveContentRepository.filter {
                it.includeGroup("io.github.helm")
            }
        }

        val helmDistribution = project.configurations.register("helmDistribution") {
            it.isCanBeResolved = true
            it.isCanBeConsumed = false
        }

        val expandHelmExecutable = project.tasks.register("expandHelmExecutable", Sync::class.java) { task ->
            task.from(project.tarTree(helmDistribution.map { it.singleFile }))
            task.into(project.layout.buildDirectory.dir("helm/executable"))
        }

        project.dependencies.addProvider(
            helmDistribution.name,
            extension.helmVersion.zip(extension.platformIdentifier, HelmArtifactDependency::helmArtifactCoordinates)
        )

        extension.helmExecutable.convention(expandHelmExecutable.zip(extension.platformIdentifier) { task, platformIdentifier -> task.destinationDir.resolve("$platformIdentifier/helm") })

        project.tasks.withType(Test::class.java) { testTask ->
            testTask.jvmArgumentProviders += FileArgumentProvider(
                "com.rrmoore.helm.test.executable.path",
                extension.helmExecutable
            )
        }
    }
}