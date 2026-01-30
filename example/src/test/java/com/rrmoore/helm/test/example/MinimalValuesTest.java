package com.rrmoore.helm.test.example;

import com.rrmoore.helm.test.HelmExecutor;
import com.rrmoore.helm.test.Workload;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class MinimalValuesTest {

    private final HelmExecutor helm = new GymRegisterHelmExecutor();

    @Test
    void rendersSuccessfullyWithNoValues() {
        helm.template();
    }

    @Test
    void everyWorkloadHasChecksumAnnotationsForItsResources() {
        var manifests = helm.template();

        var failures = manifests.findAllWorkloads().stream()
            .map(Workload::verifyChecksumAnnotations)
            .filter(it -> !it.success())
            .map(Workload.VerifyChecksumAnnotationsResult::message)
            .collect(Collectors.joining("\n"));

        assertTrue(failures.isBlank(), failures);
    }
}
