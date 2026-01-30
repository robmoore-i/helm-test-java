package com.rrmoore.helm.test;

import java.io.File;
import kotlin.text.Charsets;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@SuppressWarnings("DataFlowIssue")
public class ManifestsTest {

    private static Manifests manifests;

    @BeforeAll
    static void beforeAll() {
        var helm = new HelmExecutor(new File("src/test/resources/my-app"));
        manifests = helm.template();
    }

    @Test
    void canGetDeployment() {
        assertEquals("nginx:1.16.0", manifests.getDeployment("my-app").getSpec().getTemplate().getSpec().getContainers().getFirst().getImage());
    }

    @Test
    void canGetStatefulSet() {
        assertEquals("my-app", manifests.getStatefulSet("my-app").getSpec().getServiceName());
    }

    @Test
    void canGetJob() {
        assertEquals(30, manifests.getJob("hello-world-job").getSpec().getTtlSecondsAfterFinished());
    }

    @Test
    void canGetIngress() {
        assertEquals("/", manifests.getIngress("default-ingress").getSpec().getRules().getFirst().getHttp().getPaths().getFirst().getPath());
    }

    @Test
    void canGetService() {
        assertEquals("None", manifests.getService("my-app").getSpec().getClusterIP());
    }

    @Test
    void canGetServiceAccount() {
        assertEquals(true, manifests.getServiceAccount("my-app").getAutomountServiceAccountToken());
    }

    @Test
    void canGetConfigMap() {
        assertEquals("app-data", manifests.getConfigMap("my-app-config").getData().get("bucketName"));
        assertEquals("app-data", manifests.getConfigMapValue("my-app-config", "bucketName"));
    }

    @Test
    void canGetSecret() {
        assertEquals("password123", new String(manifests.getSecret("app-password").getData().get("password"), Charsets.UTF_8));
        assertEquals("password123", manifests.getSecretValue("app-password", "password"));
    }

    @Test
    void canGetPvc() {
        assertEquals("ReadWriteOnce", manifests.getPersistentVolumeClaim("app-data").getSpec().getAccessModes().getFirst());
    }

    @Test
    void canFindAllWorkloads() {
        var appWorkload = manifests.findAllWorkloads().stream()
            .filter(it -> it.name().equals("my-app"))
            .findFirst().orElseThrow();
        var mainContainer = appWorkload.containers().stream()
            .filter(it -> it.getName().equals("main"))
            .findFirst().orElseThrow();
        assertEquals("nginx:1.16.0", mainContainer.getImage());
    }

    @Test
    void canFindWorkload() {
        var appWorkload = manifests.findWorkload("Deployment", "my-app").orElseThrow();
        var mainContainer = appWorkload.containers().stream()
            .filter(it -> it.getName().equals("main"))
            .findFirst().orElseThrow();
        assertEquals("IfNotPresent", mainContainer.getImagePullPolicy());
    }

    @Test
    void canCompareEqualManifests() {
        var helm = new HelmExecutor(new File("src/test/resources/my-app"));
        var firstRendering = helm.template();
        var secondRendering = helm.template();
        assertEquals(firstRendering, secondRendering);
    }

    @Test
    void canCompareUnequalManifests() {
        var helm = new HelmExecutor(new File("src/test/resources/my-app"));
        var values = """
            equalityTesting:
              useRandomSecret: true
            """;

        var firstRendering = helm.template(values);
        var secondRendering = helm.template(values);
        assertNotEquals(firstRendering, secondRendering);
    }
}
