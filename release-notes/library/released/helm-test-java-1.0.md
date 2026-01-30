# Helm Test Java 1.0

- (FEATURE) Provide the `HelmExecutor` class which executes `helm template` using provided YAML values, and returns an instance of `Manifests` which exposes the rendered Kubernetes objects using the classes provided by the official Kubernetes Java client.
