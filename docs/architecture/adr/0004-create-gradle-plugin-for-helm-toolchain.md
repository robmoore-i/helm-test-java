# ADR 0004: Create Gradle Plugin for Helm Toolchain Management

## Status

Accepted

## Context

The library requires a Helm executable to function. Users need a way to specify which Helm binary to use. Several approaches were considered:

1. **Gradle plugin** to manage Helm executable
2. **Manual installation** with environment variables
3. **Bundled Helm binary** in library JAR
4. **Dynamic download** by library at runtime
5. **Assume Helm on PATH**

The core question is how to make the library easy to use while maintaining flexibility and security.

## Decision

Create a companion Gradle plugin (`com.rrmoore.gradle.helm-test-java`) that:
- Downloads specific Helm versions from official repository
- Configures test tasks with Helm executable path
- Supports manual Helm executable specification

## Rationale

**Chosen Approach (Gradle Plugin)**:

Pros:
- Declarative configuration in build files
- Automated download and setup
- Version pinning per project
- Integration with Gradle lifecycle
- Platform detection
- Supports both download and local binary modes

Cons:
- Additional plugin to maintain
- Plugin portal publishing complexity
- Gradle version compatibility requirements

**Rejected Alternatives**:

1. **Manual Installation + Environment Variables**:
   - Pros: Simple, no plugin needed
   - Cons: Setup burden on users, version inconsistencies, CI/CD complexity
   - Rejection reason: Poor user experience

2. **Bundled Binary**:
   - Pros: Zero configuration
   - Cons: Large JAR size, platform-specific packaging, version inflexibility, security concerns
   - Rejection reason: Impractical for multi-platform support

3. **Runtime Download**:
   - Pros: Automatic, no plugin
   - Cons: Network calls during tests, security risks, caching complexity, proxy issues
   - Rejection reason: Security and reliability concerns

4. **Assume PATH**:
   - Pros: Simple
   - Cons: Version inconsistencies, setup burden, CI/CD issues
   - Rejection reason: Fragile and unreliable

## Consequences

### Positive

1. **User Experience**: Single configuration, automated setup
   ```kotlin
   helmToolchain {
       helmVersion = "3.19.4"
   }
   ```

2. **Reproducibility**: Version pinned per project
3. **CI/CD Friendly**: Automatic download in CI environments
4. **Flexibility**: Supports both download and local binary modes
5. **Platform Support**: Detects and downloads correct binary

### Negative

1. **Maintenance Burden**: Two artifacts to maintain and publish
2. **Complexity**: Plugin code adds ~200 LOC
3. **Dependency**: Users must apply both library and plugin

### Mitigations

- **Publication**: Automate plugin publishing to Gradle Plugin Portal
- **Documentation**: Clear setup instructions with examples
- **Fallback**: Support library without plugin (manual Helm path)

## Implementation Details

**Plugin Configuration**:
```kotlin
helmToolchain {
    // Option 1: Download specific version
    helmVersion = "3.19.4"

    // Option 2: Use local binary
    helmExecutable = File("/usr/local/bin/helm")

    // Optional: Override platform detection
    helmPlatform = PlatformIdentifier.LINUX_AMD64
}
```

**Ivy Repository for Helm Downloads**:
```kotlin
ivyArtifactRepository.url = URI.create("https://get.helm.sh")
ivyArtifactRepository.patternLayout {
    it.artifact("[artifact]-v[revision]-[classifier].[ext]")
}
// Resolves to: https://get.helm.sh/helm-v3.19.4-darwin-arm64.tar.gz
```

**System Property Mechanism**:
```kotlin
// Plugin sets system property for test tasks
testTask.jvmArgumentProviders += FileArgumentProvider(
    "com.rrmoore.helm.test.executable.path",
    extension.helmExecutable
)

// Library reads system property
System.getProperty("com.rrmoore.helm.test.executable.path")
```

**Platform Detection**:
```kotlin
val osGuess = when {
    "mac" in osName -> "DARWIN"
    "windows" in osName -> "WINDOWS"
    else -> "LINUX"
}
val archGuess = when (osArch) {
    "x86_64", "amd64" -> "AMD64"
    else -> "ARM64"
}
```

## Related Decisions

- ADR 0001: Use Process Execution for Helm CLI Interaction
- ADR 0006: Target Java 25

## References

- Gradle Plugin Development Guide
- Helm Release Distribution
- Gradle Ivy Repository Documentation
