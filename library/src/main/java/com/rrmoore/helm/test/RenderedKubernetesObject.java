package com.rrmoore.helm.test;

import com.rrmoore.helm.test.jdkext.YamlMap;
import io.kubernetes.client.common.KubernetesObject;

public record RenderedKubernetesObject(KubernetesObject kubernetesObject, YamlMap yamlMap) {
}
