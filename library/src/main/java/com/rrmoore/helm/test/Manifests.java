package com.rrmoore.helm.test;

import io.kubernetes.client.common.KubernetesObject;
import io.kubernetes.client.openapi.models.V1Deployment;
import java.util.List;

public class Manifests {
    private final List<KubernetesObject> renderedObjects;

    public Manifests(List<KubernetesObject> renderedObjects) {
        this.renderedObjects = renderedObjects;
    }

    public V1Deployment findDeployment(String name) {
        return (V1Deployment) renderedObjects.stream()
            .filter(o ->
                o.getApiVersion().equals("apps/v1") &&
                    o.getKind().equals("Deployment") &&
                    name.equals(o.getMetadata().getName()))
            .findFirst()
            .orElse(null);

    }
}
