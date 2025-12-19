package com.rrmoore.gradle.helm.test

object HelmArtifactDependency {

    fun helmArtifactCoordinates(version: String, platformIdentifier: PlatformIdentifier) =
        helmArtifactCoordinates(version, platformIdentifier.toString())

    fun helmArtifactCoordinates(version: String, platformIdentifier: String): String {
        return "io.github.helm:helm:$version:$platformIdentifier@tar.gz"
    }
}