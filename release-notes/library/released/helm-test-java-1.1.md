# Helm Test Java 1.1

- (FEATURE) Add tools for writing automated tests which verify compatibility of your Helm chart with GitOps-based tooling, such as ArgoCD.
  - Introduce the ability to compare the equality of Manifests from separate renderings.
  - Introduce the ability to automatically verify that every workload's checksum annotations match the set of resources it references, for a given rendering.
- (INTERNAL) Create an internal package and move some classes there. This is technically a breaking change, but nobody is using this library at the moment so I have no qualms.
