package com.rrmoore.helm.test;

import com.rrmoore.helm.test.internal.jdkext.Exceptions;
import io.kubernetes.client.openapi.models.V1Deployment;
import java.io.File;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("DataFlowIssue")
public class HelmExecutorTest {

    private final HelmExecutor helm = new HelmExecutor(new HelmChart(new File("src/test/resources/my-app")));

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

        var deployment = (V1Deployment) manifests.getOne("apps/v1", "Deployment", "my-app");
        assertEquals("IfNotPresent", deployment.getSpec().getTemplate().getSpec().getContainers().getFirst().getImagePullPolicy());
    }

    @Test
    void canRenderTemplateWithValues() {
        var values = """
            image:
              pullPolicy: Always
            """;

        var manifests = helm.template(values);

        var deployment = (V1Deployment) manifests.getOne("apps/v1", "Deployment", "my-app");
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

        var deployment = (V1Deployment) manifests.getOne("apps/v1", "Deployment", "my-app");
        assertEquals("Always", deployment.getSpec().getTemplate().getSpec().getContainers().getFirst().getImagePullPolicy());
        assertEquals(2, deployment.getSpec().getReplicas());
    }

    @Test
    void canShowTemplateErrors() {
        var values = """
            image:
              pullPolicy: VeryBad
            """;

        var error = helm.templateError(values);
        assertThat(error, containsString("Don't use the VeryBad image pull policy!"));
    }

    @Test
    void canShowTemplateErrorsForMultipleValues() {
        var valuesA = """
            image:
              pullPolicy: VeryBad
            """;
        var valuesB = """
            replicas: 2
            """;

        var error = helm.templateError(List.of(valuesA, valuesB));
        assertThat(error, containsString("Don't use the VeryBad image pull policy!"));
    }

    @Test
    void showsUnexpectedTemplateErrors() {
        var values = """
            image:
              pullPolicy: VeryBad
            """;

        try {
            helm.template(values);
            assert false : "Expected an Exception to be thrown, but none was";
        } catch (Exception e) {
            assertThat(e.getMessage(), containsString("Don't use the VeryBad image pull policy!"));
        }
    }

    @Test
    void recordsRenderedManifestsFromUnexpectedTemplateSuccess() {
        Manifests manifests = null;

        try {
            helm.templateError("replicas: 1");
            assert false : "Expected an Exception to be thrown, but none was";
        } catch (Exception e) {
            var prefix = "Manifests written to file '";
            var index = e.getMessage().indexOf(prefix) + prefix.length();
            var fileName = e.getMessage().substring(index, e.getMessage().length() - 1);
            var file = new File(fileName);
            assert file.isFile() : "File " + fileName + " either is not a valid file path or does not exist.";
            manifests = Exceptions.uncheck(() -> Manifests.fromYaml(file.toPath()));
        }

        var deployment = (V1Deployment) manifests.getOne("apps/v1", "Deployment", "my-app");
        assertEquals("IfNotPresent", deployment.getSpec().getTemplate().getSpec().getContainers().getFirst().getImagePullPolicy());
    }

    @Test
    void canRenderSchemaValidationError() {
        var values = """
            image:
              unknownValue: bang
            """;

        try {
            helm.template(values);
            assert false : "Expected an Exception to be thrown, but none was";
        } catch (Exception e) {
            assertThat(e.getMessage(), containsString("at '/image': additional properties 'unknownValue' not allowed"));
        }
    }
}
