# Helm Test Java 1.0

- (FEATURE) Provide the `HelmExecutor` class which executes `helm template` using provided YAML values, and returns an instance of `Manifests` which exposes the rendered Kubernetes objects using the classes provided by the official Kubernetes Java client.

# Helm Test Gradle plugin 1.0

- (FEATURE) Provide the ability to download a particular version of `helm` from Helm's official repository, or specify and use a local `helm` executable file. The downloaded or configured `helm` executable is passed to the test process as a system property, which is used by the helm-test-java library. 
