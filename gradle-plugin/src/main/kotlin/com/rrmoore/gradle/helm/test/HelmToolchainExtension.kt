package com.rrmoore.gradle.helm.test

import javax.inject.Inject
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property

abstract class HelmToolchainExtension @Inject constructor(objects: ObjectFactory) {

    /**
     * The platform identifier (operating system and architecture) to use for downloaded Helm artifacts,
     * assuming their structure matches that of the default Helm executables.
     * This will be the case if you are either directly using or mirroring the default repository for Helm artifacts.
     */
    val platformIdentifier: Property<PlatformIdentifier> = objects.property(PlatformIdentifier::class.java)

    /**
     * The version of Helm to download and use.
     *
     * Look at the [Helm releases page on GitHub](https://github.com/helm/helm/releases) to see available versions.
     */
    val helmVersion: Property<String> = objects.property(String::class.java)
}
