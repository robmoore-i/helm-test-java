# Helm Test Java 1.1

(FEATURE) Introduce the ability to automatically verify that every workload's checksum annotations match the set of resources it references, for a given rendering. This verification is necessary to prove that your Helm chart is compatible with GitOps based tooling such as ArgoCD.
(INTERNAL) Create an internal package and move some classes there. This is technically a breaking change, but nobody is using this library at the moment so I have no qualms.

# Helm Test Gradle plugin 1.1
