package com.rrmoore.helm.test;

import java.io.File;
import java.util.Set;
import java.util.TreeSet;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HelmChartTest {

    private final HelmChart chart = new HelmChart(new File("src/test/resources/my-app"));

    @Test
    void canTemplate() {
        var manifests = chart.getHelmExecutor().template();
        assertEquals("nginx:1.16.0", manifests.getDeployment("my-app").getSpec().getTemplate().getSpec().getContainers().getFirst().getImage());
    }

    @Test
    void canReadValuesFromTemplates() {
        var values = chart.readValuesFromTemplates();
        assertEquals(EXPECTED_VALUES, values);
    }

    @Test
    void canReadValuesFromSchema() {
        var values = chart.readValuesFromSchema();
        assertEquals(EXPECTED_VALUES, values);
    }

    private static final TreeSet<String> EXPECTED_VALUES = new TreeSet<>(Set.of(
        "checksumAnnotationTest.missingConfigMapVolumeAnnotation",
        "checksumAnnotationTest.missingEnvConfigMapAnnotation",
        "checksumAnnotationTest.missingEnvFromConfigMapAnnotation",
        "checksumAnnotationTest.missingEnvFromSecretAnnotation",
        "checksumAnnotationTest.missingEnvSecretAnnotation",
        "checksumAnnotationTest.missingImagePullSecretAnnotation",
        "checksumAnnotationTest.missingSecretVolumeAnnotation",
        "checksumAnnotationTest.noAnnotations",
        "checksumAnnotationTest.unnecessaryExtraResourceAnnotation",
        "config2.enabled",
        "deeply.nested.value.here",
        "edge.first",
        "edge.second",
        "edge.useFeature",
        "equalityTesting.useRandomSecret",
        "image.pullPolicy",
        "replicas",
        "with_underscore"
    ));
}
