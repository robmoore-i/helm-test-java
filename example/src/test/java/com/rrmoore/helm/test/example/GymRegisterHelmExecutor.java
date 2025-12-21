package com.rrmoore.helm.test.example;

import com.rrmoore.helm.test.HelmExecutor;
import java.io.File;

public class GymRegisterHelmExecutor extends HelmExecutor {

    public GymRegisterHelmExecutor() {
        super(new File("src/main/helm/gym-register"));
    }
}
