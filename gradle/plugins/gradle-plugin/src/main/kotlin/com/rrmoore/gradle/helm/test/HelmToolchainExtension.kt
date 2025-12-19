package com.rrmoore.gradle.helm.test

import java.io.File
import javax.inject.Inject
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory

abstract class HelmToolchainExtension @Inject constructor(objects: ObjectFactory, providers: ProviderFactory) {

    /**
     * The platform identifier (operating system and architecture) to use for downloaded Helm artifacts,
     * assuming their structure matches that of the default Helm executables.
     * This will be the case if you are either directly using or mirroring the default repository for Helm artifacts.
     * If unset, the plugin will guess a value based on the `os.name` and `os.arch` system properties.
     */
    val platformIdentifier: Property<PlatformIdentifier> = objects.property(PlatformIdentifier::class.java)
        .convention(guessPlatformIdentifier(providers.systemProperty("os.name").zip(providers.systemProperty("os.arch"), ::Pair)))

    /**
     * The version of Helm to download and use.
     *
     * Look at the [Helm releases page on GitHub](https://github.com/helm/helm/releases) to see available versions.
     */
    val helmVersion: Property<String> = objects.property(String::class.java)

    /**
     * The executable `helm` file.
     */
    val helmExecutable: Property<File> = objects.property(File::class.java)

    companion object {

        /**
         * Performs a simplistic and not-very-good guess at the platform identifier.
         *
         * If this isn't correct for you, you can always set helmPlatform yourself by making a better calculation in your build logic.
         */
        private fun guessPlatformIdentifier(osNameAndArch: Provider<Pair<String, String>>) =
            osNameAndArch.map { (osName, osArch) ->
                val osNameLowercase = osName.lowercase()
                val osGuess = if (osNameLowercase.contains("mac")) {
                    "DARWIN"
                } else if (osNameLowercase.contains("windows")) {
                    "WINDOWS"
                } else {
                    "LINUX"
                }
                val osArchLowercase = osArch.lowercase()
                val archGuess = if (osArchLowercase == "x86_64" || osArchLowercase == "amd64") {
                    "AMD64"
                } else {
                    "ARM64"
                }
                PlatformIdentifier.valueOf("${osGuess}_$archGuess")
            }
    }
}
