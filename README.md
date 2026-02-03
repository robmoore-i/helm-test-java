# Helm Test Java

![Maven central](https://img.shields.io/maven-central/v/com.rrmoore/helm-test-java)
![Gradle plugin portal](https://img.shields.io/gradle-plugin-portal/v/com.rrmoore.gradle.helm-test-java)

A library for writing automated tests for Helm charts, so that you can have a great developer experience while working on them.
There is a companion Gradle plugin to manage the Helm toolchain you use for tests.

## Setup

### Using Gradle

Below gives the copy & pastable Kotlin Gradle code.

#### With the `java` plugin

```kotlin
plugins {
    java
    `jvm-test-suite`
    id("com.rrmoore.gradle.helm-test-java") version "1.0"
}

helmToolchain {
    helmVersion = "3.19.4"
}

dependencies {
    testImplementation("com.rrmoore:helm-test-java:1.2")
}
```

#### With the `jvm-test-suite` plugin

```kotlin
plugins {
    java
    `jvm-test-suite`
    id("com.rrmoore.gradle.helm-test-java") version "1.0"
}

helmToolchain {
    helmVersion = "3.19.4"
}

testing {
    suites {
        named<JvmTestSuite>("test") {
            dependencies {
                implementation("com.rrmoore:helm-test-java:1.2")
            }
        }
    }
}
```

## Usage of the library

These examples assume the use of the `com.rrmoore.gradle.helm-test-java` Gradle plugin to pass the location of the `helm` executable to the Java process for use by the library.

#### Verify the content of rendered templates

```java
package com.rrmoore.helm.test.example.app;

import com.rrmoore.helm.test.HelmChart;
import com.rrmoore.helm.test.HelmExecutor;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SantaSleighHelmChartTest {

    private final HelmExecutor helm = new HelmExecutor(new HelmChart(new File("src/main/helm/santa-sleigh")));

    // Run `helm template` and read values using the 
    // official Kubernetes Java client's model classes. 
    @Test
    void rudolphIsIncludedByDefault() {
        var manifests = helm.template();

        assertEquals("9", manifests.getConfigMapValue("sleigh-config", "numberOfReindeer"));
    }

    // Pass different combinations of values YAML to `helm template` 
    // to see how the templates are rendered in different conditional branches.
    @Test
    void rudolphCanBeRestedUsingFlag() {
        var values = """
            sleigh:
              rudolph:
                enabled: false
            """;

        var manifests = helm.template(values);

        assertEquals("9", manifests.getConfigMapValue("sleigh-config", "numberOfReindeer"));
        assertEquals("headlamp", manifests.getConfigMapValue("sleigh-config", "lightSource"));
    }

    // Use test-driven development to implement powerful semantic input verifications
    // which ensure that renderings are semantically coherent and correct.
    @Test
    void cannotUseRudolphsShinyNoseIfHeIsNotOnTheSleigh() {
        var values = """
            sleigh:
              rudolph:
                enabled: false
              lightSource: rudolph
            """;

        var error = helm.templateError(values);

        assertThat(error, containsString("Cannot use Rudolph's shiny nose as the sleigh's light source if he isn't on the sleigh."));
    }
}
```

#### Verify your Helm chart's compatibility with GitOps tooling

For a Helm chart to be compatible with GitOps tooling such as ArgoCD, it needs to be possible to configure the Helm chart's output to be deterministic. This is because such tools run `helm template` periodically and if the rendered output isn't exactly equal to what is stored in the tool's system of record (usually a git repository), then the manifests are updated there, and then applied to the cluster. Apps which don't correctly implement deterministic output don't function properly with these tools because the app is constantly coming up and down. Using `helm-test-java`, you can write a simple, automated tests which prove that your Helm chart supports deterministic output.

Another requirement for compatibility with GitOps tooling for Helm charts, is that each [workload resource](https://kubernetes.io/docs/concepts/workloads/) needs to recreate its pods when any config it depends on is changed. Existing pods need to be cycled and recreated when the configuration they depend on changes. Your running workloads need to reflect the set of ConfigMaps and Secrets that are actually deployed. A common way to achieve this is by adding annotations to your pod definitions so that updates to the config they depend on will cause them to rotate (see example/src/main/helm/gym-register/templates/app/deployment.yaml for an example of how to implement this pattern). It's important that these annotations are accurate and up-to-date, but staying on top of that throughout different sets of changes and updated isn't necessarily easy. Using `helm-test-java`, you can write simple, automated tests which prove that your Helm chart specifies accurate, up-to-date workload annotations.

```java
package com.rrmoore.helm.test.example.app;

import com.rrmoore.helm.test.HelmChart;
import com.rrmoore.helm.test.HelmExecutor;
import com.rrmoore.helm.test.Workload;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SantaSleighHelmChartGitOpsCompatibilityTest {

    private final HelmExecutor helm = new HelmExecutor(new HelmChart(new File("src/main/helm/santa-sleigh")));

    // Run `helm template` twice to confirm that if a secret name is specified for the credential,
    // its value won't be randomly generated and the rendered output is deterministic.
    @Test
    void deterministicOutputIsConfigurable() {
        var values = """
            credentials:
              password:
                secretName: my-custom-secret
            """;
        var manifestsA = helm.template(values);
        var manifestsB = helm.template(values);

        assertEquals(manifestsA, manifestsB);
    }
    
    // Verify that all your workloads (i.e. deployments, jobs, statefulsets and other pod-based resources) have
    // checksum annotations that are aligned with the ConfigMaps and Secrets they reference.
    @Test
    void checksumAnnotations() {
        var manifests = helm.template();

        var failures = manifests.findAllWorkloads().stream()
            .map(Workload::verifyChecksumAnnotations)
            .filter(it -> !it.success())
            .map(Workload.VerifyChecksumAnnotationsResult::message)
            .collect(Collectors.joining("\n"));

        assertTrue(failures.isBlank(), failures);
    }
}
```

#### (Experimental) Verify that your schema includes every value that you reference in your templates 

This feature is experimental because the way it reads values from your templates is not particularly clever. It is essentially looking for instances of .Values.* (ignoring parentheses). If the below caveats make this feature useless to you, that would be interesting information for me.

The template scraper has the following caveats:
- Variables are not followed. If you set {{ $x := .Values.foo }} and later use {{ $x.bar }}, this method will not detect the value 'foo.bar'.
- Method calls are not followed. If you run {{ include "myfunc" .Values.arg }} and myfunc uses {{ .subarg }}, this method will not detect the value 'arg.subarg'.
- Scoping is not considered. If you create a scope using {{ with .Values.top }} and within it use {{ .inner }}, this method will not detect the value 'top.inner'.

If your Helm chart avoids exploiting these scoping possibilities and always uses .Values.* syntax to access values, then you can use `helm-test-java` to create a simple and powerful automated test to verify the parity between your Helm chart schema and your Helm chart's templates. You could consider adding outliers manually to the test if necessary.

```java
package com.rrmoore.helm.test.example.app;

import com.rrmoore.helm.test.HelmChart;
import java.io.File;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SantaSleighHelmChartSchemaValuesParityTest {

    // Compare the values included in your values.schema.json with the values in your templates,
    // giving you the confidence to know that your schema file is accurate and up-to-date.
    @Test
    void allValuesInTemplatesAreIncludedInSchema() {
        var chart = new HelmChart(new File("src/main/helm/santa-sleigh"));
        assertEquals(chart.readValuesFromSchema(), chart.readValuesFromTemplates());
    }
}
```

## Usage of the Gradle plugin

The `com.rrmoore.gradle.helm-test-java` Gradle plugin optionally downloads the chosen version of the Helm executable from the default Helm public artifact repository and makes it available to your test code via the system property "com.rrmoore.helm.test.executable.path". It can also make the path of a known local `helm` executable available to your test code via the same means, without downloading anything.

You can configure the plugin by using the `helmToolchain` extension.

### Downloading a Helm version from the internet

To configure the Helm version to download, set `helmVersion`:

```
plugins {
    id("com.rrmoore.gradle.helm-test-java") version "1.0"
}

helmToolchain {
    helmVersion = "4.0.4"
}
```

The plugin will guess the platform (i.e. OS and CPU architecture) for the Helm binary to download, based on the JVM system properties "os.name" and "os.arch", however you can also override this or perform your own calculation to determine the platform:

```
import com.rrmoore.gradle.helm.test.PlatformIdentifier

plugins {
    id("com.rrmoore.gradle.helm-test-java") version "1.0"
}

helmToolchain {
    helmVersion = "4.0.4"
    helmPlatform = PlatformIdentifier.LINUX_AMD64
}
```

### Using a local `helm` executable

If you know where your `helm` binary is on all the machines where your build runs (e.g. on CI, on each developer's machine), then you can set its path directly, meaning the plugin will make no attempt to download a Helm binary from the internet:

```
plugins {
    id("com.rrmoore.gradle.helm-test-java") version "1.0"
}

helmToolchain {
    helmExecutable = File("/path/to/helm")
}
```

## Advanced setup

If for whatever reason, you can't use the `helm-test-java` Gradle plugin, you can still use the library. There is a constructor of `HelmExecutor` that doesn't make use of the "com.rrmoore.helm.test.executable.path" system property. You can use this constructor if you want to use the library without using the Gradle plugin.

## Request features or report bugs

Please create a GitHub issue. There may already be an issue for the feature or bug fix you need, in which case the best thing you can do is give it a thumbs up and leave a comment on it.

## Contribute

Go for it! There isn't much code so it shouldn't be too hard. Alternatively, make a GitHub issue describing what you need and I'll have a look.
