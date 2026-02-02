package com.rrmoore.helm.test;

import java.io.File;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WorkloadTest {

    private final HelmExecutor helm = new HelmExecutor(new HelmChart(new File("src/test/resources/my-app")));

    @Test
    void verifiesValidChecksumAnnotations() {
        var manifests = helm.template();

        var workload = manifests.getWorkload("Deployment", "checksum-annotation-tester");
        var result = workload.verifyChecksumAnnotations();
        assertTrue(result.success());
    }

    @Test
    void reportsMissingChecksumAnnotationForEnvConfigMap() {
        var values = """
            checksumAnnotationTest:
              missingEnvConfigMapAnnotation: true
            """;
        var manifests = helm.template(values);

        var workload = manifests.getWorkload("Deployment", "checksum-annotation-tester");
        var result = workload.verifyChecksumAnnotations();
        assertFalse(result.success());
        assertEquals("Workload 'checksum-annotation-tester' is missing checksum annotation for referenced ConfigMap 'checksum-annotation-tester-config'.", result.message());
    }

    @Test
    void reportsMissingChecksumAnnotationForEnvSecret() {
        var values = """
            checksumAnnotationTest:
              missingEnvSecretAnnotation: true
            """;
        var manifests = helm.template(values);

        var workload = manifests.getWorkload("Deployment", "checksum-annotation-tester");
        var result = workload.verifyChecksumAnnotations();
        assertFalse(result.success());
        assertEquals("Workload 'checksum-annotation-tester' is missing checksum annotation for referenced Secret 'checksum-annotation-tester-secret'.", result.message());
    }

    @Test
    void reportsMissingChecksumAnnotationForImagePullSecret() {
        var values = """
            checksumAnnotationTest:
              missingImagePullSecretAnnotation: true
            """;
        var manifests = helm.template(values);

        var workload = manifests.getWorkload("Deployment", "checksum-annotation-tester");
        var result = workload.verifyChecksumAnnotations();
        assertFalse(result.success());
        assertEquals("Workload 'checksum-annotation-tester' is missing checksum annotation for referenced Secret 'my-registry-credentials'.", result.message());
    }

    @Test
    void reportsMissingChecksumAnnotationForConfigMapVolume() {
        var values = """
            checksumAnnotationTest:
              missingConfigMapVolumeAnnotation: true
            """;
        var manifests = helm.template(values);

        var workload = manifests.getWorkload("Deployment", "checksum-annotation-tester");
        var result = workload.verifyChecksumAnnotations();
        assertFalse(result.success());
        assertEquals("Workload 'checksum-annotation-tester' is missing checksum annotation for referenced ConfigMap 'volume-config'.", result.message());
    }

    @Test
    void reportsMissingChecksumAnnotationForSecretVolume() {
        var values = """
            checksumAnnotationTest:
              missingSecretVolumeAnnotation: true
            """;
        var manifests = helm.template(values);

        var workload = manifests.getWorkload("Deployment", "checksum-annotation-tester");
        var result = workload.verifyChecksumAnnotations();
        assertFalse(result.success());
        assertEquals("Workload 'checksum-annotation-tester' is missing checksum annotation for referenced Secret 'volume-secret'.", result.message());
    }

    @Test
    void reportsMissingChecksumAnnotationForEnvFromConfigMap() {
        var values = """
            checksumAnnotationTest:
              missingEnvFromConfigMapAnnotation: true
            """;
        var manifests = helm.template(values);

        var workload = manifests.getWorkload("Deployment", "checksum-annotation-tester");
        var result = workload.verifyChecksumAnnotations();
        assertFalse(result.success());
        assertEquals("Workload 'checksum-annotation-tester' is missing checksum annotation for referenced ConfigMap 'envfrom-config'.", result.message());
    }

    @Test
    void reportsMissingChecksumAnnotationForEnvFromSecret() {
        var values = """
            checksumAnnotationTest:
              missingEnvFromSecretAnnotation: true
            """;
        var manifests = helm.template(values);

        var workload = manifests.getWorkload("Deployment", "checksum-annotation-tester");
        var result = workload.verifyChecksumAnnotations();
        assertFalse(result.success());
        assertEquals("Workload 'checksum-annotation-tester' is missing checksum annotation for referenced Secret 'envfrom-secret'.", result.message());
    }

    @Test
    void reportsMissingAnnotationsWhenNoAnnotationsExist() {
        var values = """
            checksumAnnotationTest:
              noAnnotations: true
            """;
        var manifests = helm.template(values);

        var workload = manifests.getWorkload("Deployment", "checksum-annotation-tester");
        var result = workload.verifyChecksumAnnotations();
        assertFalse(result.success());
        assertTrue(result.message().contains("Workload 'checksum-annotation-tester' is missing checksum annotation for referenced ConfigMap 'checksum-annotation-tester-config'."));
        assertTrue(result.message().contains("Workload 'checksum-annotation-tester' is missing checksum annotation for referenced Secret 'checksum-annotation-tester-secret'."));
    }

    @Test
    void reportsUnnecessaryExtraChecksumAnnotation() {
        var values = """
            checksumAnnotationTest:
              unnecessaryExtraResourceAnnotation: true
            """;
        var manifests = helm.template(values);

        var workload = manifests.getWorkload("Deployment", "checksum-annotation-tester");
        var result = workload.verifyChecksumAnnotations();
        assertFalse(result.success());
        assertEquals("Workload 'checksum-annotation-tester' has unnecessary extra checksum annotation 'checksum/checksum-annotation-tester-extra-config'.", result.message());
    }

    @Test
    void reportsMultipleIssuesWhenBothMissingAndUnnecessaryAnnotationsExist() {
        var values = """
            checksumAnnotationTest:
              missingEnvConfigMapAnnotation: true
              unnecessaryExtraResourceAnnotation: true
            """;
        var manifests = helm.template(values);

        var workload = manifests.getWorkload("Deployment", "checksum-annotation-tester");
        var result = workload.verifyChecksumAnnotations();
        assertFalse(result.success());
        assertTrue(result.message().contains("Workload 'checksum-annotation-tester' is missing checksum annotation for referenced ConfigMap 'checksum-annotation-tester-config'."));
        assertTrue(result.message().contains("Workload 'checksum-annotation-tester' has unnecessary extra checksum annotation 'checksum/checksum-annotation-tester-extra-config'."));
    }
}
