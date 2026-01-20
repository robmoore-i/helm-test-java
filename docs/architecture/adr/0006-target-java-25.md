# ADR 0006: Target Java 25 as Minimum Version

## Status

Accepted

## Context

The library must choose a minimum Java version. This affects:
- Available language features
- User adoption (older Java versions more common)
- Dependency compatibility
- Long-term maintenance

Options considered:
1. **Java 8** - Widest compatibility
2. **Java 11** - Current LTS
3. **Java 17** - Latest LTS
4. **Java 21** - Latest LTS with modern features
5. **Java 25** - Latest release

The core question is balancing modern features with user compatibility.

## Decision

Target Java 25 as the minimum supported version.

## Rationale

**Chosen Approach (Java 25)**:

Pros:
- Latest language features (records, pattern matching, etc.)
- Modern standard library APIs
- Virtual threads for future concurrency
- Text blocks for YAML literals
- Enhanced switch expressions

Cons:
- Smaller user base (many still on Java 8/11/17)
- Requires users to upgrade
- Shorter track record for stability

**Rejected Alternatives**:

1. **Java 8**:
   - Pros: Maximum compatibility
   - Cons: Missing language improvements, verbose code, missing modern APIs
   - Rejection reason: Testing library can require modern Java

2. **Java 11/17**:
   - Pros: LTS, wider adoption, stable
   - Cons: Missing recent language improvements
   - Rejection reason: Benefits of modern features outweigh compatibility

3. **Java 21**:
   - Pros: Latest LTS, modern features
   - Cons: Similar tradeoffs to Java 25
   - Rejection reason: If requiring upgrade, use latest

## Consequences

### Positive

1. **Modern Syntax**: Clean, readable code
   ```java
   // Text blocks for YAML
   var values = """
       app:
         name: my-app
         replicas: 3
       """;

   // Records for data
   private record StdProcessOutput(String stdout, String stderr) {}
   ```

2. **Better APIs**: Modern stream and Optional APIs
3. **Future-Proof**: Room to adopt new features
4. **Smaller Codebase**: Less boilerplate

### Negative

1. **Limited Adoption**: Users on older Java cannot use library
2. **Enterprise Barrier**: Large orgs slower to upgrade
3. **CI/CD Updates**: Users must update CI Java versions

### Mitigations

- **Documentation**: Clearly state Java 25 requirement
- **Error Messages**: Fail fast with clear Java version error
- **Justification**: Document why modern Java chosen

## Implementation Details

**Build Configuration**:
```kotlin
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}
```

**Modern Language Features Used**:

1. **Text Blocks**:
   ```java
   var values = """
       database:
         host: localhost
       """;
   ```

2. **Records**:
   ```java
   private record StdProcessOutput(String stdout, String stderr) {}
   ```

3. **Switch Expressions**:
   ```java
   var platform = switch(os) {
       case "mac" -> "DARWIN";
       case "windows" -> "WINDOWS";
       default -> "LINUX";
   };
   ```

4. **Enhanced NullPointerException**:
   - Clear messages showing exactly which variable was null

**Version Validation**:
```kotlin
// Gradle enforces version
tasks.withType<JavaCompile> {
    options.release = 25
}
```

## User Impact

**Migration Path for Users**:

1. **Update Java version**:
   ```bash
   # Via SDKMAN
   sdk install java 25-tem
   sdk use java 25-tem
   ```

2. **Update CI**:
   ```yaml
   # GitHub Actions
   - uses: actions/setup-java@v3
     with:
       java-version: '25'
   ```

3. **Update Gradle**:
   ```kotlin
   java {
       toolchain {
           languageVersion = JavaLanguageVersion.of(25)
       }
   }
   ```

**Alternative for Older Java Users**:
- Fork and backport to older Java (not officially supported)
- Use containerized tests with Java 25
- Upgrade Java version (recommended)

## Related Decisions

- ADR 0004: Create Gradle Plugin for Helm Toolchain (uses Kotlin)

## References

- Java 25 Release Notes
- JEP Index (Java Enhancement Proposals)
- Gradle Toolchain documentation
