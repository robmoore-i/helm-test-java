package com.rrmoore.gradle.helm.test

import java.io.File
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.process.CommandLineArgumentProvider

class FileArgumentProvider(
    @Input val propertyName: String,
    @PathSensitive(PathSensitivity.NONE) @InputFile val inputFile: Provider<File>
) : CommandLineArgumentProvider {

    override fun asArguments(): Iterable<String?> {
        return listOf("-D$propertyName=${inputFile.get().path}")
    }
}
