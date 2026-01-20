# Architecture Decision Records

This directory contains Architecture Decision Records (ADRs) for Helm Test Java.

## What is an ADR?

An Architecture Decision Record (ADR) captures an important architectural decision made along with its context and consequences.

## ADR Format

Each ADR follows this structure:
- **Title**: Short descriptive title
- **Status**: Proposed, Accepted, Deprecated, Superseded
- **Context**: What is the issue that we're seeing that is motivating this decision?
- **Decision**: What is the change that we're actually proposing or doing?
- **Rationale**: Why this decision over alternatives?
- **Consequences**: What becomes easier or harder as a result?

## Index of ADRs

| ADR | Title | Status |
|-----|-------|--------|
| [0001](0001-use-process-execution-for-helm-cli.md) | Use Process Execution for Helm CLI Interaction | Accepted |
| [0002](0002-use-temporary-files-for-values-yaml.md) | Use Temporary Files for Values YAML | Accepted |
| [0003](0003-use-kubernetes-java-client-for-manifest-parsing.md) | Use Kubernetes Java Client for Manifest Parsing | Accepted |
| [0004](0004-create-gradle-plugin-for-helm-toolchain.md) | Create Gradle Plugin for Helm Toolchain Management | Accepted |
| [0005](0005-provide-decorator-pattern-for-custom-manifest-access.md) | Provide Decorator Pattern for Custom Manifest Access | Accepted |
| [0006](0006-target-java-25.md) | Target Java 25 as Minimum Version | Accepted |

## Decision Relationships

```
ADR 0001 (Process Execution)
├── ADR 0002 (Temporary Files) - How to pass values
├── ADR 0003 (K8s Java Client) - How to parse output
└── ADR 0004 (Gradle Plugin) - How to provide Helm binary

ADR 0003 (K8s Java Client)
└── ADR 0005 (Decorator Pattern) - How to extend manifest access

ADR 0004 (Gradle Plugin)
└── ADR 0006 (Java 25) - Language features for implementation
```

## Adding New ADRs

When adding a new ADR:

1. Use the next sequential number (0007, 0008, etc.)
2. Follow the naming convention: `NNNN-short-title.md`
3. Use the template structure (Status, Context, Decision, Rationale, Consequences)
4. Update this README with the new entry
5. Link related ADRs in the "Related Decisions" section

## Template

```markdown
# ADR NNNN: [Title]

## Status

[Proposed | Accepted | Deprecated | Superseded]

## Context

[What is the issue motivating this decision?]

## Decision

[What is the change we're proposing?]

## Rationale

[Why this approach over alternatives?]

**Chosen Approach**:
- Pros: ...
- Cons: ...

**Rejected Alternatives**:
1. Alternative 1:
   - Pros: ...
   - Cons: ...
   - Rejection reason: ...

## Consequences

### Positive
1. ...

### Negative
1. ...

### Mitigations
- ...

## Related Decisions

- ADR XXXX: ...

## References

- ...
```
