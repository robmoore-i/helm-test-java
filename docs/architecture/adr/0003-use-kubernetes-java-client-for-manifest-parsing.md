# ADR 0003: Use Kubernetes Java Client for Manifest Parsing

## Status

Accepted

## Context

After executing `helm template`, the library receives YAML output containing Kubernetes manifests. This output must be parsed into Java objects for test assertions. Several parsing approaches were considered:

1. **Kubernetes Java Client** - Official Kubernetes client with model classes
2. **Custom YAML parsing** with generic Map/List structures
3. **Lightweight YAML parser** (SnakeYAML, Jackson YAML) with custom models
4. **String-based assertions** without parsing

The core question is how to provide type-safe, usable access to rendered Kubernetes resources.

## Decision

Use the official Kubernetes Java Client (`io.kubernetes:client-java`) to parse and represent Kubernetes manifests.

## Rationale

**Chosen Approach (Kubernetes Java Client)**:

Pros:
- Official Kubernetes model classes (V1Deployment, V1Service, etc.)
- Type-safe API with compile-time checking
- Comprehensive coverage of all Kubernetes resource types
- Built-in YAML deserialization
- Active maintenance and updates for new K8s versions
- IDE autocomplete and documentation

Cons:
- Large dependency (~10MB, 50+ transitive dependencies)
- Includes client functionality not needed (API server communication)
- Version must match target Kubernetes version

**Rejected Alternatives**:

1. **Custom YAML Parsing**:
   - Pros: Lightweight, no dependencies, full control
   - Cons: Must maintain Kubernetes model classes, type safety requires significant code, K8s API evolution burden
   - Rejection reason: Unsustainable maintenance burden

2. **Generic YAML Structures**:
   - Pros: Minimal dependencies, flexible
   - Cons: No type safety, verbose assertions (`map.get("spec").get("replicas")`), fragile to structure changes
   - Rejection reason: Poor developer experience

3. **String-Based Assertions**:
   - Pros: No parsing needed, simple
   - Cons: Fragile, hard to read, cannot validate structure, breaks on formatting changes
   - Rejection reason: Insufficient for robust testing

## Consequences

### Positive

1. **Type Safety**: Compile-time checking for resource access
   ```java
   V1Deployment deployment = manifests.getDeployment("my-app");
   Integer replicas = deployment.getSpec().getReplicas();
   ```

2. **Discoverability**: IDE autocomplete for all Kubernetes fields
3. **Correctness**: Official models guarantee correct structure
4. **Maintenance**: No need to track Kubernetes API changes manually
5. **Extensibility**: Users can access any Kubernetes resource type

### Negative

1. **Dependency Size**: Adds ~10MB to library dependencies
2. **Transitive Dependencies**: Brings 50+ dependencies
3. **Version Coupling**: Must update client for new K8s API versions
4. **Startup Time**: Additional classes to load (minimal impact)

### Mitigations

- **Dependency Size**: Acceptable for testing library (not runtime dependency)
- **Version Coupling**: Document supported K8s versions
- **API Scope**: Mark as `api` dependency so users can override version

## Implementation Details

**Parsing Multi-Document YAML**:
```java
var renderedObjects = Arrays.stream(yaml.split("---"))
    .skip(1)  // Skip empty first element
    .map(doc -> (KubernetesObject) Yaml.load(doc))
    .toList();
```

**Type-Safe Access**:
```java
public V1Deployment getDeployment(String name) {
    return getOne("apps/v1", "Deployment", name, V1Deployment.class);
}
```

**Generic Access**:
```java
public <T extends KubernetesObject> T getOne(
    String apiVersion,
    String kind,
    String name,
    Class<T> clazz
) {
    return clazz.cast(getOne(apiVersion, kind, name));
}
```

**Extensibility Pattern**:
```java
// Users can access any resource type
V1CustomResourceDefinition crd = manifests.getOne(
    "apiextensions.k8s.io/v1",
    "CustomResourceDefinition",
    "my-crd",
    V1CustomResourceDefinition.class
);
```

## Related Decisions

- ADR 0001: Use Process Execution for Helm CLI Interaction
- ADR 0005: Provide Decorator Pattern for Custom Manifest Access

## References

- Kubernetes Java Client: https://github.com/kubernetes-client/java
- Kubernetes API documentation
- SnakeYAML documentation (used internally by client)
