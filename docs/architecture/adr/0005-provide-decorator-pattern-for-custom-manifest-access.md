# ADR 0005: Provide Decorator Pattern for Custom Manifest Access

## Status

Accepted

## Context

The `Manifests` class provides generic access to rendered Kubernetes resources. However, different Helm charts have different resource types and naming conventions. The library must decide:

1. **Comprehensive built-in accessors** for all possible K8s resources
2. **Minimal accessors** with extensibility mechanism
3. **Only generic access** (no convenience methods)

The core question is how to balance library maintainability with user convenience.

## Decision

Provide a minimal set of common resource accessors in `Manifests`, and explicitly encourage users to create decorator classes for chart-specific access patterns.

## Rationale

**Chosen Approach (Decorator Pattern)**:

Pros:
- Library remains focused (10-15 built-in accessors)
- Users add chart-specific logic without library changes
- Type-safe access to custom resources
- Encapsulates chart knowledge in test code
- No library version dependency for new resource types

Cons:
- Users must write decorator classes
- Some code duplication across projects
- Learning curve for decorator pattern

**Rejected Alternatives**:

1. **Comprehensive Built-In Accessors**:
   - Pros: Everything included, no user code needed
   - Cons: 100+ methods, constant updates for new K8s resources, bloated API
   - Rejection reason: Unmaintainable and excessive

2. **Only Generic Access**:
   - Pros: Minimal library code, maximum flexibility
   - Cons: Verbose test code, no type safety for common cases, poor discoverability
   - Rejection reason: Poor user experience

3. **Code Generation**:
   - Pros: Automated chart-specific accessors
   - Cons: Build complexity, unclear generated code, debugging difficulties
   - Rejection reason: Overcomplicated for benefit provided

## Consequences

### Positive

1. **Library Simplicity**: Core class ~160 LOC
2. **Stability**: No need to update for every K8s resource type
3. **Flexibility**: Users control their accessor API
4. **Encapsulation**: Chart knowledge stays in test code
5. **Type Safety**: Maintained via decorators

### Negative

1. **Initial Setup**: Users write decorator class per chart
2. **Documentation Need**: Must explain decorator pattern
3. **Code Duplication**: Common accessors repeated across projects

### Mitigations

- **Documentation**: Explicit Javadoc recommendation to decorate
- **Examples**: Example module demonstrates decorator pattern
- **Built-In Basics**: Provide accessors for most common resource types

## Implementation Details

**Manifests Class (Built-In Accessors)**:
```java
public class Manifests {
    // Common resources
    public V1Deployment getDeployment(String name) { }
    public V1Service getService(String name) { }
    public V1ConfigMap getConfigMap(String name) { }
    public V1Secret getSecret(String name) { }
    // ... 10-15 common types

    // Generic access for everything else
    public <T> T getOne(String api, String kind, String name, Class<T> clazz) { }
}
```

**User Decorator Pattern**:
```java
public class GymRegisterManifests extends Manifests {
    public GymRegisterManifests(List<KubernetesObject> objects) {
        super(objects);
    }

    // Chart-specific accessors
    public V1Deployment getAppDeployment() {
        return getDeployment("gym-register-app");
    }

    public V1Deployment getDatabaseDeployment() {
        return getDeployment("gym-register-database");
    }

    public String getDatabasePassword() {
        return getSecretValue("database-superuser-credentials", "password");
    }

    // Custom resource types
    public MyCustomResource getCustomConfig() {
        return getOne("my.api/v1", "CustomConfig", "config", MyCustomResource.class);
    }
}
```

**Factory Pattern for Decorator**:
```java
public class GymRegisterHelmExecutor extends HelmExecutor {
    public GymRegisterManifests template(String values) {
        var manifests = super.template(values);
        return new GymRegisterManifests(manifests.findAll(o -> true));
    }
}
```

**Javadoc Guidance**:
```java
/**
 * Represents the rendered Kubernetes manifests created by `helm template`.
 *
 * You should create a decorator for this class which encapsulates
 * specific information about the Helm chart you're testing.
 * In your decorator, you can add methods to get different kinds of
 * rendered Kubernetes objects, which are not covered exhaustively in this class.
 */
public class Manifests { }
```

## Related Decisions

- ADR 0003: Use Kubernetes Java Client for Manifest Parsing

## References

- Gang of Four Decorator Pattern
- Effective Java: Favor composition over inheritance
- Example module: GymRegisterManifests implementation
