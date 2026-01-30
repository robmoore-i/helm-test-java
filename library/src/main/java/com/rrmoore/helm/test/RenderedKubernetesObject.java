package com.rrmoore.helm.test;

import com.rrmoore.helm.test.internal.jdkext.YamlMap;
import io.kubernetes.client.common.KubernetesObject;
import java.util.Objects;

public record RenderedKubernetesObject(KubernetesObject kubernetesObject, YamlMap yamlMap) {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RenderedKubernetesObject that)) return false;
        return Objects.equals(yamlMap, that.yamlMap);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(yamlMap);
    }
}
