package com.rrmoore.helm.test;

import java.io.File;
import kotlin.text.Charsets;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
