package com.rrmoore.helm.test;

import io.kubernetes.client.common.KubernetesObject;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Represents the rendered Kubernetes manifests created by `helm template`.
 * <p>
 * You should create a decorator for this class which encapsulates specific information about the Helm chart you're testing.
 * You can also add convenient methods to your decorator such as getDeployment, getService etc. to minimise boilerplate.
 */
public class Manifests {

    private final List<KubernetesObject> renderedObjects;

    public Manifests(List<KubernetesObject> renderedObjects) {
        this.renderedObjects = renderedObjects;
    }

    /**
     * @return All the rendered Kubernetes objects matching the provided predicate.
     */
    public List<KubernetesObject> findAll(Predicate<KubernetesObject> predicate) {
        return renderedObjects.stream()
            .filter(predicate)
            .toList();
    }

    /**
     * @return The one rendered Kubernetes object matching the provided predicate, or Optional.empty() otherwise.
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
     * @return The one rendered Kubernetes object matching the provided predicate, throws otherwise.
     * @throws java.lang.IllegalArgumentException if either zero or more than one Kubernetes object match the predicate.
     *                                            Use {@link #findOne(java.util.function.Predicate)} if it's acceptable for no objects to match the predicate.
     *                                            Use {@link #findAll(java.util.function.Predicate)} if you have no strict expectations about how many objects match the predicate.
     */
    public KubernetesObject getOne(Predicate<KubernetesObject> predicate) {
        return findOne(predicate)
            .orElseThrow(() -> new IllegalArgumentException("No rendered Kubernetes object matches the provided predicate"));
    }

    /**
     * @return The one rendered Kubernetes object with the provided Kind and Name, throws otherwise.
     * @throws java.lang.IllegalArgumentException if either zero or more than one Kubernetes object match.
     *                                            Use {@link #findOne(java.util.function.Predicate)} if it's acceptable for no objects to match.
     *                                            Use {@link #findAll(java.util.function.Predicate)} if you have no strict expectations about how many objects match.
     */
    public KubernetesObject getOne(String apiVersion, String kind, String name) {
        return getOne(o -> o.getApiVersion().equals(apiVersion) && o.getKind().equals(kind) && name.equals(o.getMetadata().getName()));
    }
}
