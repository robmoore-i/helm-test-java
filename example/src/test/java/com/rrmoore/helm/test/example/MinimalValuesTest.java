package com.rrmoore.helm.test.example;

import com.rrmoore.helm.test.HelmExecutor;
import org.junit.jupiter.api.Test;

public class MinimalValuesTest {

    private final HelmExecutor helm = new GymRegisterHelmExecutor();

    @Test
    void rendersSuccessfullyWithNoValues() {
        helm.template();
    }
}
