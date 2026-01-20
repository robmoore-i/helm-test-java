# Security Architecture

## Security Overview

Helm Test Java is a testing library that executes local processes and reads local files. The security model centers on process execution controls, file system access validation, dependency integrity, and supply chain security.

**Threat Model Context**:
- **Runtime Environment**: Developer workstations and CI/CD systems
- **Privilege Level**: User-level permissions (no elevation required)
- **Network Access**: Outbound only for dependency downloads
- **Data Sensitivity**: Test fixtures and rendered manifests (non-production data)

## Security Controls

### Process Execution Security

#### Command Injection Prevention

**Risk**: Malicious input in values YAML or chart paths could manipulate command execution.

**Mitigations**:
1. **No Shell Invocation**: Uses ProcessBuilder directly, not shell
2. **Argument Separation**: Command and arguments passed as separate list elements
3. **No String Interpolation**: Paths and values never interpolated into command string
4. **File-Based Values**: Values passed via files, not command-line arguments

**Safe Pattern**:
```java
var command = List.of(
    helmExecutable.getAbsolutePath(),
    "template",
    chart.getAbsolutePath(),
    "--values",
    valuesFile.getAbsolutePath()
);
new ProcessBuilder(command).start();
```

#### Process Timeout

All process executions limited to 10 seconds to prevent infinite hangs and mitigate denial-of-service via slow templates.

### File System Security

**Path Validation**: Validates Helm executable existence, executability, and chart directory existence before processing.

**Temporary File Security**: Uses system temp directory with unique file names to prevent collisions. Files inherit default user permissions with no encryption at rest.

### Dependency Security

**Supply Chain Security**:
- Fixed versions (no dynamic ranges)
- Trusted repositories (Maven Central, Gradle Plugin Portal)
- HTTPS only for all downloads
- GPG verification for Maven artifacts

**Recommendations**:
- Use dependency verification in Gradle
- Pin transitive dependencies
- Scan dependencies with OWASP Dependency-Check

### Secret Management

**Philosophy**: Tests should not use real secrets. Use placeholder values for testing.

**Recommended Pattern**:
```java
var values = """
    secrets:
      apiKey: "test-key-not-real"
    """;
```

**Anti-Pattern**:
```java
var values = """
    secrets:
      apiKey: "${System.getenv("REAL_API_KEY")}"
    """;
```

## Security Best Practices

### For Library Users

1. Use test fixtures only (no real credentials)
2. Validate chart sources
3. Isolate test environments
4. Monitor dependencies regularly
5. Keep library and Helm versions current

### For Plugin Users

1. Pin specific Helm versions
2. Enable Gradle dependency verification
3. Use local binaries in air-gapped environments

## Vulnerability Disclosure

**Contact**: GitHub Security Advisories at https://github.com/robmoore-i/helm-test-java/security

**Process**:
1. Report via GitHub private vulnerability reporting
2. Do not open public issues for security vulnerabilities
3. Allow 90 days for remediation before public disclosure

## Known Limitations

1. No input sanitization for values YAML
2. Temp files not explicitly deleted
3. Process resource limits (timeout only, no CPU/memory limits)
4. Transitive dependency vulnerabilities not controlled
