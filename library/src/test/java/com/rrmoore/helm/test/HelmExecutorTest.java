package com.rrmoore.helm.test;

import java.io.File;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("DataFlowIssue")
public class HelmExecutorTest {

    private final HelmExecutor helm = new HelmExecutor(new File("src/test/resources/my-app"));

    @Test
    void canGetVersion() {
        assertEquals(
            "version.BuildInfo{Version:\"v3.19.4\", GitCommit:\"7cfb6e486dac026202556836bb910c37d847793e\", GitTreeState:\"clean\", GoVersion:\"go1.24.11\"}",
            helm.version()
        );
    }

    @Test
    void canRenderTemplateWithoutValues() {
        var manifests = helm.template();

        var deployment = manifests.findDeployment("my-app");

        assertEquals("IfNotPresent", deployment.getSpec().getTemplate().getSpec().getContainers().getFirst().getImagePullPolicy());
    }

    @Test
    void canRenderTemplateWithValues() {
        var values = """
            image:
              pullPolicy: Always
            """;
        var manifests = helm.template(values);

        var deployment = manifests.findDeployment("my-app");

        assertEquals("Always", deployment.getSpec().getTemplate().getSpec().getContainers().getFirst().getImagePullPolicy());
    }

    @Test
    void canRenderTemplateWithMultipleValues() {
        var valuesA = """
            image:
              pullPolicy: Always
            """;
        var valuesB = """
            replicas: 2
            """;
        var manifests = helm.template(List.of(valuesA, valuesB));

        var deployment = manifests.findDeployment("my-app");

        assertEquals("Always", deployment.getSpec().getTemplate().getSpec().getContainers().getFirst().getImagePullPolicy());
        assertEquals(2, deployment.getSpec().getReplicas());
    }
}
