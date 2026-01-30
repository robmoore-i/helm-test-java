package com.rrmoore.helm.test;

import com.rrmoore.helm.test.jdkext.YamlMap;
import io.kubernetes.client.openapi.models.V1Container;
import io.kubernetes.client.util.Yaml;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class Workload {

    private static final org.yaml.snakeyaml.Yaml SNAKE_YAML = new org.yaml.snakeyaml.Yaml();
    static final Set<String> WORKLOAD_KINDS = Set.of("Deployment", "StatefulSet", "ReplicaSet", "Job", "CronJob", "DaemonSet");

    private final RenderedKubernetesObject renderedKubernetesObject;

    public Workload(RenderedKubernetesObject renderedKubernetesObject) {
        checkKind(renderedKubernetesObject.kubernetesObject().getKind());
        this.renderedKubernetesObject = renderedKubernetesObject;
    }

    public static void checkKind(String kind) {
        if (!WORKLOAD_KINDS.contains(kind)) {
            throw new IllegalArgumentException("Kind '" + kind + "' is not recognised as a workload kind.");
        }
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

    /**
     * Verifies that the workload has the correct set of checksum annotations to ensure that its pods will be cycled
     * whenever a ConfigMap or Secret they reference is changed.
     * <p>
     * First, this method finds every ConfigMap or Secret referenced by the workload in all its containers' env
     * and imagePullSecret, and all the workload's volumes that are created from a ConfigMap or Secret.
     * This set of resources is then compared to the set of annotations defined by the workload under
     * spec.template.metadata.annotations. This method verifies that every referenced resource has a corresponding
     * checksum annotation i.e. there exists an annotation whose name contains both "checksum" and the name of the resource.
     * Furthermore, if there are any checksum annotations which don't contain the name of any referenced resource, they
     * are unnecessary - these are also recorded by the return value of this method.
     * <p>
     * If any resource is referenced but does not have a corresponding checksum annotation, the returned result will include
     * a message describing what's missing.
     * If any checksum annotation exists without a corresponding referenced resource, the return result's message will include
     * a message describing what's extraneous.
     * It may be that a workload has both missing and extraneous checksum annotations, in which case the message will include both.
     * <p>
     * Note: The checksum itself is entirely ignored by this method.
     */
    public VerifyChecksumAnnotationsResult verifyChecksumAnnotations() {
        var referencedConfigMaps = new HashSet<String>();
        var referencedSecrets = new HashSet<String>();

        // Find referenced ConfigMaps and Secrets from containers' env
        var containersList = renderedKubernetesObject.yamlMap().getNestedList("spec.template.spec.containers")
            .orElse(List.of());
        for (Object containerObj : containersList) {
            if (containerObj instanceof Map<?, ?> containerMap) {
                var container = new YamlMap(castToStringObjectMap(containerMap));
                collectReferencesFromContainer(container, referencedConfigMaps, referencedSecrets);
            }
        }

        // Find referenced Secrets from imagePullSecrets
        var imagePullSecretsList = renderedKubernetesObject.yamlMap().getNestedList("spec.template.spec.imagePullSecrets")
            .orElse(List.of());
        for (Object secretObj : imagePullSecretsList) {
            if (secretObj instanceof Map<?, ?> secretMap) {
                var name = (String) secretMap.get("name");
                if (name != null) {
                    referencedSecrets.add(name);
                }
            }
        }

        // Find referenced ConfigMaps and Secrets from volumes
        var volumesList = renderedKubernetesObject.yamlMap().getNestedList("spec.template.spec.volumes")
            .orElse(List.of());
        for (Object volumeObj : volumesList) {
            if (volumeObj instanceof Map<?, ?> volumeMap) {
                var volume = new YamlMap(castToStringObjectMap(volumeMap));
                volume.getNestedString("configMap.name").ifPresent(referencedConfigMaps::add);
                volume.getNestedString("secret.secretName").ifPresent(referencedSecrets::add);
            }
        }

        // Get annotations from spec.template.metadata.annotations
        var annotationsOpt = renderedKubernetesObject.yamlMap().getNested("spec.template.metadata.annotations");
        Map<String, String> annotations = Map.of();
        if (annotationsOpt.isPresent() && annotationsOpt.get() instanceof Map<?, ?> annotationsMap) {
            annotations = castToStringStringMap(annotationsMap);
        }

        // Find checksum annotations
        var checksumAnnotationsKeys = new HashSet<String>();
        for (String annotationKey : annotations.keySet()) {
            if (annotationKey.toLowerCase().contains("checksum")) {
                checksumAnnotationsKeys.add(annotationKey);
            }
        }

        // Check for missing checksum annotations
        var messages = new ArrayList<String>();

        for (String configMapName : referencedConfigMaps) {
            if (checksumAnnotationsKeys.stream().noneMatch(it -> it.contains(configMapName))) {
                messages.add("Missing checksum annotation for referenced ConfigMap '" + configMapName + "'.");
            }
        }

        for (String secretName : referencedSecrets) {
            if (checksumAnnotationsKeys.stream().noneMatch(ann -> ann.contains(secretName))) {
                messages.add("Missing checksum annotation for referenced Secret '" + secretName + "'.");
            }
        }

        // Check for unnecessary checksum annotations
        for (String annotationKey : checksumAnnotationsKeys) {
            if (Stream.concat(referencedConfigMaps.stream(), referencedSecrets.stream()).noneMatch(annotationKey::contains)) {
                messages.add("Unnecessary extra checksum annotation '" + annotationKey + "'.");
            }
        }

        if (messages.isEmpty()) {
            return VerifyChecksumAnnotationsResult.SUCCESS;
        }
        return new VerifyChecksumAnnotationsResult(false, String.join(" ", messages));
    }

    private void collectReferencesFromContainer(YamlMap container, Set<String> configMaps, Set<String> secrets) {
        var envList = container.getNestedList("env").orElse(List.of());
        for (Object envObj : envList) {
            if (envObj instanceof Map<?, ?> envMap) {
                var env = new YamlMap(castToStringObjectMap(envMap));
                env.getNestedString("valueFrom.configMapKeyRef.name").ifPresent(configMaps::add);
                env.getNestedString("valueFrom.secretKeyRef.name").ifPresent(secrets::add);
            }
        }

        var envFromList = container.getNestedList("envFrom").orElse(List.of());
        for (Object envFromObj : envFromList) {
            if (envFromObj instanceof Map<?, ?> envFromMap) {
                var envFrom = new YamlMap(castToStringObjectMap(envFromMap));
                envFrom.getNestedString("configMapRef.name").ifPresent(configMaps::add);
                envFrom.getNestedString("secretRef.name").ifPresent(secrets::add);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> castToStringObjectMap(Map<?, ?> map) {
        return (Map<String, Object>) map;
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> castToStringStringMap(Map<?, ?> map) {
        return (Map<String, String>) map;
    }

    public record VerifyChecksumAnnotationsResult(boolean success, String message) {

        public static VerifyChecksumAnnotationsResult SUCCESS = new VerifyChecksumAnnotationsResult(true, "");
    }
}
