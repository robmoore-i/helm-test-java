package com.rrmoore.helm.test;

import java.io.File;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WorkloadTest {

    private final HelmExecutor helm = new HelmExecutor(new File("src/test/resources/my-app"));

    @Test
    void canVerifyValidChecksumAnnotations() {
        var manifests = helm.template();

        var workload = manifests.getWorkload("Deployment", "checksum-annotation-tester");
        var result = workload.verifyChecksumAnnotations();
        assertTrue(result.success());
    }

    @Test
    void reportsMissingChecksumAnnotationForReferencedConfigMap() {
        var values = """
            checksumAnnotationTest:
              missingConfigMapAnnotation: true
            """;
        var manifests = helm.template(values);

        var workload = manifests.getWorkload("Deployment", "checksum-annotation-tester");
        var result = workload.verifyChecksumAnnotations();
        assertFalse(result.success());
        assertEquals("Missing checksum annotation for referenced ConfigMap 'checksum-annotation-tester-config'.", result.message());
    }

    @Test
    void reportsMissingChecksumAnnotationForReferencedSecret() {
        var values = """
            checksumAnnotationTest:
              missingSecretAnnotation: true
            """;
        var manifests = helm.template(values);

        var workload = manifests.getWorkload("Deployment", "checksum-annotation-tester");
        var result = workload.verifyChecksumAnnotations();
        assertFalse(result.success());
        assertEquals("Missing checksum annotation for referenced Secret 'checksum-annotation-tester-secret'.", result.message());
    }

    @Test
    void reportsUnnecessaryExtraChecksumAnnotationForReferencedResource() {
        var values = """
            checksumAnnotationTest:
              unnecessaryExtraResourceAnnotation: true
            """;
        var manifests = helm.template(values);

        var workload = manifests.getWorkload("Deployment", "checksum-annotation-tester");
        var result = workload.verifyChecksumAnnotations();
        assertFalse(result.success());
        assertEquals("Unnecessary extra checksum annotation 'checksum/checksum-annotation-tester-extra-config'.", result.message());
    }
}
