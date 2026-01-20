# ADR 0002: Use Temporary Files for Values YAML

## Status

Accepted

## Context

Tests need to pass custom values to Helm's `template` command. Helm supports values via:

1. **Files** (`--values file.yaml` or `-f file.yaml`)
2. **Inline values** (`--set key=value`)
3. **Stdin** (reading from process stdin)

The library provides values as Java strings in test methods. These must be converted to a format Helm can consume.

## Decision

Write values YAML strings to temporary files and pass file paths to Helm via `--values` arguments.

## Rationale

**Chosen Approach (Temporary Files)**:

Pros:
- Supports multi-line YAML with complex structures
- Preserves YAML formatting and comments
- No escaping needed for special characters
- Helm natively designed for file-based values
- Supports multiple values files (Helm merging)
- No command-line length limits

Cons:
- File I/O overhead (minimal for small values)
- Temp file management required
- Disk space usage (negligible)

**Rejected Alternatives**:

1. **--set Arguments**:
   - Pros: No file I/O, simpler
   - Cons: Cannot represent complex YAML (nested objects, arrays), requires escaping, fragile for special characters
   - Rejection reason: Insufficient for real-world chart testing

2. **Stdin Piping**:
   - Pros: No file system interaction
   - Cons: Helm doesn't support reading values from stdin, complex ProcessBuilder setup
   - Rejection reason: Not supported by Helm

3. **Persistent Values Files**:
   - Pros: Reusable across tests
   - Cons: Test isolation issues, cleanup complexity, file naming conflicts
   - Rejection reason: Breaks test independence

## Consequences

### Positive

1. **Expressiveness**: Full YAML support in test values
2. **Multiple Values**: Can pass multiple values files per test
3. **Debugging**: Can inspect temp files if needed
4. **Helm Compatibility**: Uses Helm's primary values mechanism

### Negative

1. **File I/O Overhead**: ~1-5ms per test
2. **Cleanup Dependency**: Relies on OS temp directory cleanup
3. **Disk Space**: Temp files persist until OS cleanup

### Mitigations

- **Performance**: Overhead negligible for testing use case
- **Cleanup**: Document OS temp cleanup behavior
- **Naming**: Use timestamp + random suffix to prevent collisions

## Implementation Details

**File Creation**:
```java
var timestamp = formatter.format(initTimestamp);
var valuesFile = File.createTempFile(
    "helm-test-values-yaml-" + timestamp + "-",
    ".yaml"
);
Files.writeString(valuesFile.toPath(), valuesYaml);
```

**Naming Convention**:
```
helm-test-values-yaml-20250120173045-a8f9d3b2.yaml
                       ^^^^^^^^^^^^^ ^^^^^^^^^
                       timestamp     random
```

**Multiple Values Files**:
```java
return valuesYamls.stream()
    .flatMap(yaml -> {
        var file = createTempFile(yaml);
        return Stream.of("--values", file.getAbsolutePath());
    })
    .toList();
// Result: ["--values", "file1.yaml", "--values", "file2.yaml"]
```

## Related Decisions

- ADR 0001: Use Process Execution for Helm CLI Interaction

## References

- Helm values documentation
- Java File.createTempFile documentation
- Helm values merging precedence
