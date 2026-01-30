package com.rrmoore.helm.test;

import com.rrmoore.helm.test.jdkext.YamlMap;
import io.kubernetes.client.openapi.models.V1Container;
import io.kubernetes.client.util.Yaml;
import java.util.ArrayList;
import java.util.Arrays;
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
        checkKind(renderedKubernetesObject.kubernetesObject().getKind(),
            renderedKubernetesObject.kubernetesObject().getMetadata().getName());
        this.renderedKubernetesObject = renderedKubernetesObject;
    }

    public static void checkKind(String kind, String... context) {
        if (!WORKLOAD_KINDS.contains(kind)) {
            var contextSuffix = context.length == 0 ? "" : " (" + Arrays.toString(context) + ")";
            throw new IllegalArgumentException("Kind '" + kind + "' is not recognised as a workload kind." + contextSuffix);
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
     * Note: The correctness of the checksum itself is not verified by this method.
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

        // Find checksum annotations from spec.template.metadata.annotations
        var checksumAnnotationsKeys = new HashSet<String>();
        var maybeAnnotations = renderedKubernetesObject.yamlMap().getNested("spec.template.metadata.annotations");
        if (maybeAnnotations.isPresent() && maybeAnnotations.get() instanceof Map<?, ?> annotationsMap) {
            //noinspection unchecked
            var annotations = (Map<String, String>) annotationsMap;
            for (String annotationKey : annotations.keySet()) {
                if (annotationKey.toLowerCase().contains("checksum")) {
                    checksumAnnotationsKeys.add(annotationKey);
                }
            }
        }

        // Check for missing checksum annotations
        var messages = new ArrayList<String>();
        for (String configMapName : referencedConfigMaps) {
            if (checksumAnnotationsKeys.stream().noneMatch(it -> it.contains(configMapName))) {
                messages.add("Workload '" + name() + "' is missing checksum annotation for referenced ConfigMap '" + configMapName + "'.");
            }
        }
        for (String secretName : referencedSecrets) {
            if (checksumAnnotationsKeys.stream().noneMatch(it -> it.contains(secretName))) {
                messages.add("Workload '" + name() + "' is missing checksum annotation for referenced Secret '" + secretName + "'.");
            }
        }

        // Check for unnecessary checksum annotations
        for (String annotationKey : checksumAnnotationsKeys) {
            if (Stream.concat(referencedConfigMaps.stream(), referencedSecrets.stream()).noneMatch(annotationKey::contains)) {
                messages.add("Workload '" + name() + "' has unnecessary extra checksum annotation '" + annotationKey + "'.");
            }
        }

        return messages.isEmpty()
            ? VerifyChecksumAnnotationsResult.SUCCESS
            : new VerifyChecksumAnnotationsResult(false, String.join("\n", messages));
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

    public record VerifyChecksumAnnotationsResult(boolean success, String message) {

        public static VerifyChecksumAnnotationsResult SUCCESS = new VerifyChecksumAnnotationsResult(true, "");
    }
}
