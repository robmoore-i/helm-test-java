package com.rrmoore.helm.test;

import io.kubernetes.client.openapi.models.V1Container;
import io.kubernetes.client.util.Yaml;
import java.util.List;
import java.util.Set;

public class Workload {

    private static final org.yaml.snakeyaml.Yaml SNAKE_YAML = new org.yaml.snakeyaml.Yaml();
    static final Set<String> WORKLOAD_KINDS = Set.of("Deployment", "StatefulSet", "ReplicaSet", "Job", "CronJob", "DaemonSet");

    private final RenderedKubernetesObject renderedKubernetesObject;

    public Workload(RenderedKubernetesObject renderedKubernetesObject) {
        var kind = renderedKubernetesObject.kubernetesObject().getKind();
        if (!WORKLOAD_KINDS.contains(kind)) {
            throw new IllegalArgumentException("Kind '" + kind + "' is not recognised as a workload kind.");
        }
        this.renderedKubernetesObject = renderedKubernetesObject;
    }

    public String name() {
        return renderedKubernetesObject.kubernetesObject().getMetadata().getName();
    }

    public List<V1Container> containers() {
        var containers = renderedKubernetesObject.yamlMap().getNestedList("spec.template.spec.containers")
            .orElseThrow(() -> new IllegalStateException("Workload " + renderedKubernetesObject.kubernetesObject().getMetadata().getName() + " does not define any containers"));
        return containers.stream()
            .map(container -> Yaml.loadAs(SNAKE_YAML.dump(container), V1Container.class))
            .toList();
    }
}
