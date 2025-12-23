# Helm Test Java

A library for writing automated tests for Helm charts, so that you can have a great developer experience while working on them.
There is also a companion Gradle plugin to further streamline the experience.

## Usage example

This example assumes the use of the `com.rrmoore.gradle.helm-test-java` Gradle plugin to pass the location of the `helm` executable to the Java process for use by the library.

```java
package com.rrmoore.helm.test.example.app;

import com.rrmoore.helm.test.HelmExecutor;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SantaSleighHelmChartTest {

    private final HelmExecutor helm = new HelmExecutor(new File("src/main/helm/santa-sleigh"));

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

## The `helm-test-java` Gradle plugin

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

## Setup using Gradle

Below gives the copy & pastable Kotlin Gradle code.

### With the `java` plugin

```
plugins {
    java
    `jvm-test-suite`
    id("com.rrmoore.gradle.helm-test-java") version "1.0"
}

helmToolchain {
    helmVersion = "3.19.4"
}

dependencies {
    testImplementation("com.rrmoore:helm-test-java:1.0")
}
```

### With the `jvm-test-suite` plugin

```
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
                implementation("com.rrmoore:helm-test-java:1.0")
            }
        }
    }
}
```

### Advanced setup

#### Without using the `helm-test-java` Gradle plugin

There is a constructor of `HelmExecutor` that doesn't make use of the "com.rrmoore.helm.test.executable.path" system property. You should use this constructor if you want to use the library without using the Gradle plugin.

## Request features or report bugs

Please create a GitHub issue.

## Contribute

Go for it! There isn't much code so it shouldn't be too hard. Alternatively, make a GitHub issue describing what you need.
