package com.rrmoore.helm.test;

import com.rrmoore.helm.test.jdkext.Exceptions;
import com.rrmoore.helm.test.jdkext.YamlMap;
import io.kubernetes.client.common.KubernetesObject;
import io.kubernetes.client.openapi.models.V1ConfigMap;
import io.kubernetes.client.openapi.models.V1Deployment;
import io.kubernetes.client.openapi.models.V1Ingress;
import io.kubernetes.client.openapi.models.V1Job;
import io.kubernetes.client.openapi.models.V1PersistentVolumeClaim;
import io.kubernetes.client.openapi.models.V1Secret;
import io.kubernetes.client.openapi.models.V1Service;
import io.kubernetes.client.openapi.models.V1ServiceAccount;
import io.kubernetes.client.openapi.models.V1StatefulSet;
import io.kubernetes.client.util.Yaml;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import kotlin.text.Charsets;

import static com.rrmoore.helm.test.Workload.WORKLOAD_KINDS;

/**
 * Represents the rendered Kubernetes manifests created by `helm template`.
 * <p>
 * You should create a decorator for this class which encapsulates specific information about the Helm chart you're testing.
 * In your decorator, you can add methods to get different kinds of rendered Kubernetes objects, which are not covered exhaustively in this class.
 */
public class Manifests {

    private final List<RenderedKubernetesObject> renderedObjects;

    public Manifests(List<RenderedKubernetesObject> renderedObjects) {
        this.renderedObjects = renderedObjects;
    }

    /**
     * @param yaml A YAML string representing any number of Kubernetes objects.
     * @return An instance of Manifests representing the Kubernetes objects defined in the provided YAML.
     */
    public static Manifests fromYaml(String yaml) {
        var renderedObjects = Arrays.stream(yaml.split("---"))
            .skip(1)
            .map(kubernetesResourceYaml -> new RenderedKubernetesObject(
                Exceptions.uncheck(() -> (KubernetesObject) Yaml.load(kubernetesResourceYaml)),
                new YamlMap(kubernetesResourceYaml)
            ))
            .toList();
        return new Manifests(renderedObjects);
    }

    /**
     * @param path A YAML file containing any number of Kubernetes objects.
     * @return An instance of Manifests representing the Kubernetes objects defined in the provided YAML file.
     */
    public static Manifests fromYaml(Path path) throws IOException {
        return fromYaml(Files.readString(path));
    }

    /**
     * @return All the rendered Kubernetes objects matching the provided predicate.
     */
    public List<KubernetesObject> findAll(Predicate<KubernetesObject> predicate) {
        return renderedObjects.stream()
            .map(RenderedKubernetesObject::kubernetesObject)
            .filter(predicate)
            .toList();
    }

    /**
     * @return The one rendered Kubernetes object matching the provided predicate if there is one, or Optional.empty() otherwise.
     * @throws java.lang.IllegalArgumentException if more than one Kubernetes object matches the predicate.
     *                                            Use {@link #findAll(java.util.function.Predicate)} if you have no strict expectations about how many objects match the predicate.
     */
    public Optional<KubernetesObject> findOne(Predicate<KubernetesObject> predicate) {
        var objects = findAll(predicate);
        if (objects.size() > 1) {
            throw new IllegalArgumentException("Expected at most one rendered Kubernetes object to match the provided predicate, but found " + objects.size());
        }
        return objects.stream().findFirst();
    }

    /**
     * @return The one rendered Kubernetes object matching the provided predicate.
     * @throws java.lang.IllegalArgumentException if either zero or more than one Kubernetes object match the predicate.
     *                                            Use {@link #findOne(java.util.function.Predicate)} if it's acceptable for no objects to match the predicate.
     *                                            Use {@link #findAll(java.util.function.Predicate)} if you have no strict expectations about how many objects match the predicate.
     */
    public KubernetesObject getOne(Predicate<KubernetesObject> predicate) {
        return findOne(predicate)
            .orElseThrow(() -> new IllegalArgumentException("No rendered Kubernetes object matches the provided predicate"));
    }

    /**
     * @return The one rendered Kubernetes object with the provided apiVersion, kind and name.
     * @throws java.lang.IllegalArgumentException if either zero or more than one Kubernetes object match.
     *                                            Use {@link #findOne(java.util.function.Predicate)} if it's acceptable for no objects to match.
     *                                            Use {@link #findAll(java.util.function.Predicate)} if you have no strict expectations about how many objects match.
     */
    public KubernetesObject getOne(String apiVersion, String kind, String name) {
        return getOne(o -> o.getApiVersion().equals(apiVersion) && o.getKind().equals(kind) && name.equals(o.getMetadata().getName()));
    }

    /**
     * @return The one rendered Kubernetes object with the provided apiVersion, kind and name, and casts it to the provided KubernetesObject subtype.
     * @throws java.lang.IllegalArgumentException if either zero or more than one Kubernetes object match.
     *                                            Use {@link #findOne(java.util.function.Predicate)} if it's acceptable for no objects to match.
     *                                            Use {@link #findAll(java.util.function.Predicate)} if you have no strict expectations about how many objects match.
     */
    public <T extends KubernetesObject> T getOne(String apiVersion, String kind, String name, Class<T> clazz) {
        return clazz.cast(getOne(apiVersion, kind, name));
    }

    // Not intended to be exhaustive.
    // Decorate Manifests to get a more exhaustive interface.

    public V1Deployment getDeployment(String name) {
        return getOne("apps/v1", "Deployment", name, V1Deployment.class);
    }

    public V1StatefulSet getStatefulSet(String name) {
        return getOne("apps/v1", "StatefulSet", name, V1StatefulSet.class);
    }

    public V1Job getJob(String name) {
        return getOne("batch/v1", "Job", name, V1Job.class);
    }

    public V1Ingress getIngress(String name) {
        return getOne("networking.k8s.io/v1", "Ingress", name, V1Ingress.class);
    }

    public V1Service getService(String name) {
        return getOne("v1", "Service", name, V1Service.class);
    }

    public V1ServiceAccount getServiceAccount(String name) {
        return getOne("v1", "ServiceAccount", name, V1ServiceAccount.class);
    }

    public V1ConfigMap getConfigMap(String name) {
        return getOne("v1", "ConfigMap", name, V1ConfigMap.class);
    }

    public String getConfigMapValue(String configMapName, String dataKey) {
        var dataMap = Objects.requireNonNull(getConfigMap(configMapName).getData(),
            "ConfigMap " + configMapName + " has no data");
        return Objects.requireNonNull(dataMap.get(dataKey), "ConfigMap " + configMapName + " has no data under key " + dataKey);
    }

    public V1Secret getSecret(String name) {
        return getOne("v1", "Secret", name, V1Secret.class);
    }

    public String getSecretValue(String secretName, String dataKey) {
        var dataMap = Objects.requireNonNull(getSecret(secretName).getData(),
            "Secret " + secretName + " has no data");
        var data = Objects.requireNonNull(dataMap.get(dataKey), "Secret " + secretName + " has no data under key " + dataKey);
        return new String(data, Charsets.UTF_8);
    }

    public V1PersistentVolumeClaim getPersistentVolumeClaim(String name) {
        return getOne("v1", "PersistentVolumeClaim", name, V1PersistentVolumeClaim.class);
    }

    public List<Workload> findAllWorkloads() {
        return renderedObjects.stream()
            .filter(it -> WORKLOAD_KINDS.contains(it.kubernetesObject().getKind()))
            .map(Workload::new)
            .toList();
    }

    public Optional<Workload> findWorkload(String kind, String name) {
        Workload.checkKind(kind, name);
        return renderedObjects.stream()
            .filter(it -> name.equals(it.kubernetesObject().getMetadata().getName()) && it.kubernetesObject().getKind().equals(kind))
            .map(Workload::new)
            .findFirst();
    }

    public Workload getWorkload(String kind, String name) {
        return findWorkload(kind, name)
            .orElseThrow(() -> new IllegalArgumentException("No rendered Kubernetes workload object matches the provided predicate (workloads are defined here: https://kubernetes.io/docs/concepts/workloads)."));
    }
}
