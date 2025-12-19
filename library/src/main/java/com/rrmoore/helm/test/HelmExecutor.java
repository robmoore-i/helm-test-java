package com.rrmoore.helm.test;

import com.rrmoore.helm.test.jdkext.Exceptions;
import io.kubernetes.client.common.KubernetesObject;
import io.kubernetes.client.util.Yaml;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Wraps the Helm executable in a usable interface for writing automated tests.
 */
public class HelmExecutor {

    private final File helmExecutable;
    private final File chart;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuuMMddHHmmss");

    /**
     * Creates a Helm executor, determining the path to the Helm executable file using a JVM system property,
     * which is set automatically by applying the helm-test-java Gradle plugin.
     */
    public HelmExecutor(File chart) {
        this(
            new File(Objects.requireNonNull(
                System.getProperty("com.rrmoore.helm.test.executable.path"),
                "Missing system property for determining the path to the Helm executable downloaded by the helm-test-java Gradle plugin. " +
                    "Is the plugin applied to this Gradle build?"
            )),
            chart
        );
    }

    /**
     * Creates a Helm executor, using the provided Helm executable File i.e. the runnable `helm` binary file.
     */
    public HelmExecutor(File helmExecutable, File chart) {
        if (!helmExecutable.isFile()) {
            throw new IllegalArgumentException("Helm executable file '" + helmExecutable.getAbsolutePath() + "' does not exist.");
        } else if (!helmExecutable.canExecute() && !helmExecutable.setExecutable(true)) {
            throw new IllegalArgumentException("Helm executable file '" + helmExecutable.getAbsolutePath() + "' is not executable, and failed in an attempt to set it to be executable.");
        }
        if (!chart.exists()) {
            throw new IllegalArgumentException("Helm chart '" + chart.getAbsolutePath() + "' does not exist.");
        }
        this.helmExecutable = helmExecutable;
        this.chart = chart;
    }

    /**
     * Runs `helm version`
     */
    public String version() {
        return executeHelm(List.of("version"));
    }

    private String executeHelm(List<String> args) {
        var command = new ArrayList<>(List.of(helmExecutable.getAbsolutePath()));
        command.addAll(args);
        try {
            var process = new ProcessBuilder(command).start();
            process.waitFor(Duration.ofSeconds(1));
            try (BufferedReader bufferedReader = process.inputReader()) {
                var output = String.join("\n", bufferedReader.readAllLines());
                int exitCode = process.exitValue();
                if (exitCode != 0) {
                    throw new RuntimeException("Command '" + String.join(" ", command) + "' finished with exit code " + exitCode + ".");
                }
                return output;
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Helm execution failed for command '" + String.join(" ", command) + "'", e);
        }
    }

    public Manifests template() {
        return executeHelmTemplate(List.of());
    }

    public Manifests template(String valuesYaml) {
        return template(List.of(valuesYaml));
    }

    public Manifests template(List<String> valuesYamls) {
        var timestamp = formatter.format(Instant.now().atZone(ZoneOffset.UTC));
        var valuesArgs = valuesYamls
            .stream()
            .flatMap(valuesYaml -> {
                var valuesFile = Exceptions.uncheck(() -> File.createTempFile("helm-test-values-yaml-" + timestamp + "-", ".yaml"));
                Exceptions.uncheck(() -> Files.writeString(valuesFile.toPath(), valuesYaml));
                return Stream.of("--values", valuesFile.getAbsolutePath());
            })
            .toList();
        return executeHelmTemplate(valuesArgs);
    }

    private Manifests executeHelmTemplate(List<String> args) {
        var helmArgs = new ArrayList<>(List.of("template", chart.getAbsolutePath()));
        helmArgs.addAll(args);
        var output = executeHelm(helmArgs);
        var renderedObjects = Arrays.stream(output.split("---"))
            .skip(1)
            .map(yaml -> Exceptions.uncheck(() -> (KubernetesObject) Yaml.load(yaml)))
            .toList();
        return new Manifests(renderedObjects);
    }
}
