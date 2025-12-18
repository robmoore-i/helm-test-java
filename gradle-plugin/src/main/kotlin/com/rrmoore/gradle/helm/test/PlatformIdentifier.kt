package com.rrmoore.gradle.helm.test

enum class PlatformIdentifier {
    DARWIN_AMD64,
    DARWIN_ARM64,
    LINUX_AMD64,
    LINUX_ARM,
    LINUX_ARM64,
    LINUX_I386("linux-386"),
    LINUX_LOONG64,
    LINUX_PPC64LE,
    LINUX_S390X,
    LINUX_RISCV64,
    WINDOWS_AMD64,
    WINDOWS_ARM64;

    private val stringValue: String

    constructor() {
        this.stringValue = name.lowercase().replace("_", "-")
    }

    constructor(stringValue: String) {
        this.stringValue = stringValue
    }


    override fun toString(): String {
        return stringValue
    }
}
