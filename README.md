# Helm Test Java

A library for writing automated tests for Helm charts, so that you can have a great developer experience while working on them.

#### The initial release is still in a work in progress, as indicated by the below TODO.

TODO:
- Release library as 1.0.
- Publish the plugin to the Gradle plugin portal as 1.0.

## Usage example

This example assumes the use of the `com.rrmoore.gradle.helm-test-java` to pass the location of the `helm` executable to the Java process for use by the library.

```
package com.rrmoore.helm.test.example.app;

import com.rrmoore.helm.test.HelmExecutor;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MyAppHelmChartTest {

    private final HelmExecutor helm = new HelmExecutor(new File("src/main/helm/my-app"));

    @Test
    void someFeatureConfigIsSetToFooByDefault() {
        var manifests = helm.template();

        assertEquals("foo", manifests.getConfigMapValue("my-app-config", "someFeature"));
    }

    @Test
    void canDisableSomeFeature() {
        var values = """
            someFeature:
              mode: disabled
            """;

        var manifests = helm.template(values);

        assertEquals("disabled", manifests.getConfigMapValue("gym-register-app-config", "someFeature"));
    }

    @Test
    void failsIfUnknownValueSpecified() {
        var values = """
            someFeature:
              mode: jibberish
            """;

        var error = helm.templateError(values);

        assertThat(error, containsString("Unrecognised someFeature mode 'jibberish'. Set someFeature.mode to one of [foo disabled] and try again."));
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
