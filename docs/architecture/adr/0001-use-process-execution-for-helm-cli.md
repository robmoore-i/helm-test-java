# ADR 0001: Use Process Execution for Helm CLI Interaction

## Status

Accepted

## Context

The library needs to execute Helm's `template` command to render chart manifests for testing. Several approaches were considered:

1. **Direct process execution** of the Helm CLI binary
2. **Helm SDK/Library** integration (if available)
3. **Shell script wrapper** around Helm commands
4. **REST API** to a Helm server

The core question is how to interact with Helm in a way that is reliable, maintainable, and provides the full feature set of Helm templating.

## Decision

Use Java's `ProcessBuilder` to directly execute the Helm CLI binary as a separate process.

## Rationale

**Chosen Approach (Process Execution)**:

Pros:
- Access to all Helm features without reimplementation
- No dependency on Helm internals
- Version independence (works with any Helm CLI version)
- Clear separation of concerns
- Standard Java API (no additional dependencies)
- Simple error handling via exit codes
- Familiar debugging (can run same commands manually)

Cons:
- Process startup overhead (~200ms)
- Need to manage Helm binary installation
- Platform-specific binary handling
- No structured error reporting (plain text stderr)

**Rejected Alternatives**:

1. **Helm SDK/Library**:
   - Pros: Faster execution, structured APIs
   - Cons: No official Java SDK, tight coupling to Helm version, maintenance burden
   - Rejection reason: No official SDK exists

2. **Shell Script Wrapper**:
   - Pros: Flexibility in command composition
   - Cons: Command injection risks, platform-specific syntax, harder to test
   - Rejection reason: Security concerns and platform portability

3. **Helm Server API**:
   - Pros: Network-based, language-agnostic
   - Cons: Requires Helm server deployment, network dependency, added complexity
   - Rejection reason: Helm doesn't provide a template-only server mode

## Consequences

### Positive

1. **Stability**: Not affected by Helm internal API changes
2. **Completeness**: All Helm template features available (functions, includes, etc.)
3. **Simplicity**: Straightforward implementation (~100 LOC)
4. **Debugging**: Can reproduce issues by running commands manually
5. **Security**: ProcessBuilder prevents shell injection when used correctly

### Negative

1. **Performance**: Process startup adds latency to each test
2. **Binary Management**: Need Gradle plugin to manage Helm executable
3. **Error Parsing**: Must parse human-readable stderr for error details
4. **Timeout Handling**: Need to prevent hanging processes

### Mitigations

- **Performance**: Accept overhead as acceptable for testing use case
- **Binary Management**: Create Gradle plugin to download/configure Helm
- **Error Parsing**: Provide full stderr in exception messages
- **Timeout**: Implement 10-second timeout per execution

## Implementation Details

**Process Execution Pattern**:
```java
var command = List.of(
    helmExecutable.getAbsolutePath(),
    "template",
    chart.getAbsolutePath(),
    "--values",
    valuesFile.getAbsolutePath()
);
var process = new ProcessBuilder(command).start();
process.waitFor(Duration.ofSeconds(10));
```

**Security Considerations**:
- Arguments passed as list (no shell interpolation)
- File paths validated before execution
- No user input in command strings

## Related Decisions

- ADR 0002: Use Temporary Files for Values YAML
- ADR 0004: Create Gradle Plugin for Helm Toolchain

## References

- Java ProcessBuilder documentation
- Helm CLI documentation
- OWASP Command Injection Prevention
