package com.rrmoore.helm.test.example.app;

import com.rrmoore.helm.test.HelmExecutor;
import com.rrmoore.helm.test.example.GymRegisterHelmExecutor;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class WalkInPolicyTest {

    private final HelmExecutor helm = new GymRegisterHelmExecutor();

    @Test
    void walkInsAreConfiguredWithConfigMap() {
        var manifests = helm.template();

        var mainContainer = manifests.getDeployment("gym-register-app").getSpec().getTemplate().getSpec().getContainers().getFirst();
        var envVar = mainContainer.getEnv().stream().filter(env -> env.getName().equals("GYM_REGISTER_APP__ALLOW_WALK_INS")).findFirst().orElseThrow();
        var configMapKeyRef = envVar.getValueFrom().getConfigMapKeyRef();
        assertEquals("gym-register-app-config", configMapKeyRef.getName());
        assertEquals("allowWalkIns", configMapKeyRef.getKey());
    }

    @Test
    void walkInsAreAllowedByDefault() {
        var manifests = helm.template();

        assertEquals("true", manifests.getConfigMapValue("gym-register-app-config", "allowWalkIns"));
    }

    @Test
    void canDisallowWalkIns() {
        var values = """
            gymRegisterApp:
              walkInPolicy: disallow
            """;

        var manifests = helm.template(values);

        assertEquals("false", manifests.getConfigMapValue("gym-register-app-config", "allowWalkIns"));
    }

    @Test
    void failsIfUnknownWalkInPolicySpecified() {
        var values = """
            gymRegisterApp:
              walkInPolicy: jibberish
            """;

        var error = helm.templateError(values);

        assertThat(error, containsString("Unrecognised walk-in policy 'jibberish'. Set gymRegisterApp.walkInPolicy to one of [allow disallow] and try again."));
    }
}
