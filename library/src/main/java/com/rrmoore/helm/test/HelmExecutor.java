package com.rrmoore.helm.test;

import com.rrmoore.helm.test.internal.jdkext.Exceptions;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Wraps the Helm executable in a usable interface for writing automated tests.
 */
public class HelmExecutor {

    private final File helmExecutable;
    private final HelmChart chart;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuuMMddHHmmss");
    private final ZonedDateTime initTimestamp = Instant.now().atZone(ZoneOffset.UTC);

    /**
     * Creates a Helm executor, determining the path to the Helm executable file using a JVM system property,
     * which is set automatically by applying the helm-test-java Gradle plugin.
     */
    public HelmExecutor(HelmChart chart) {
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
    public HelmExecutor(File helmExecutable, HelmChart chart) {
        if (!helmExecutable.isFile()) {
            throw new IllegalArgumentException("Helm executable file '" + helmExecutable.getAbsolutePath() + "' does not exist.");
        } else if (!helmExecutable.canExecute() && !helmExecutable.setExecutable(true)) {
            throw new IllegalArgumentException("Helm executable file '" + helmExecutable.getAbsolutePath() + "' is not executable, and failed in an attempt to set it to be executable.");
        }
        this.helmExecutable = helmExecutable;
        this.chart = chart;
    }

    /**
     * Creates a Helm executor, using the provided Helm executable File i.e. the runnable `helm` binary file.
     */
    public HelmExecutor(File helmExecutable, File chart) {
        this(helmExecutable, new HelmChart(chart));
    }

    /**
     * Runs `helm version`
     */
    public String version() {
        return executeHelmForOutput(List.of("version"));
    }

    /**
     * Runs `helm template`
     *
     * @return Parsed, rendered Kubernetes manifests.
     */
    public Manifests template() {
        return executeHelmTemplate(List.of());
    }

    /**
     * Runs `helm template`, passing in the provided YAML-formatted values.
     *
     * @return Parsed, rendered Kubernetes manifests.
     */
    public Manifests template(String valuesYaml) {
        return template(List.of(valuesYaml));
    }

    /**
     * Runs `helm template`, passing in all the provided YAML-formatted values.
     *
     * @return Parsed, rendered Kubernetes manifests.
     */
    public Manifests template(List<String> valuesYamls) {
        return executeHelmTemplate(templateValuesArgs(valuesYamls));
    }

    /**
     * Runs `helm template`, passing in the provided YAML-formatted values, with the expectation that it will fail.
     *
     * @return The error output of the `helm` process.
     */
    public String templateError(String valuesYaml) {
        return templateError(List.of(valuesYaml));
    }

    /**
     * Runs `helm template`, passing in all the provided YAML-formatted values, with the expectation that it will fail.
     *
     * @return The error output of the `helm` process.
     */
    public String templateError(List<String> valuesYamls) {
        var helmArgs = new ArrayList<>(List.of("template", chart.getFile().getAbsolutePath()));
        helmArgs.addAll(templateValuesArgs(valuesYamls));
        return executeHelmForError(helmArgs);
    }

    private List<String> templateValuesArgs(List<String> valuesYamls) {
        var timestamp = formatter.format(initTimestamp);
        return valuesYamls.stream()
            .flatMap(valuesYaml -> {
                var valuesFile = Exceptions.uncheck(() -> File.createTempFile("helm-test-values-yaml-" + timestamp + "-", ".yaml"));
                Exceptions.uncheck(() -> Files.writeString(valuesFile.toPath(), valuesYaml));
                return Stream.of("--values", valuesFile.getAbsolutePath());
            })
            .toList();
    }

    private Manifests executeHelmTemplate(List<String> args) {
        var helmArgs = new ArrayList<>(List.of("template", chart.getFile().getAbsolutePath()));
        helmArgs.addAll(args);
        var output = executeHelmForOutput(helmArgs);
        return Manifests.fromYaml(output);
    }

    private String executeHelmForOutput(List<String> args) {
        return executeHelm(args, Process::inputReader, true).stdout();
    }

    private String executeHelmForError(List<String> args) {
        return executeHelm(args, Process::errorReader, false).stderr();
    }

    private StdProcessOutput executeHelm(List<String> args, Function<Process, BufferedReader> processReader, boolean expectSuccess) {
        var command = new ArrayList<>(List.of(helmExecutable.getAbsolutePath()));
        command.addAll(args);
        BufferedReader inputReader = null;
        BufferedReader errorReader = null;
        try {
            var process = new ProcessBuilder(command).start();
            process.waitFor(Duration.ofSeconds(10));
            inputReader = process.inputReader();
            errorReader = process.errorReader();
            var stdout = String.join("\n", inputReader.readAllLines());
            var stderr = String.join("\n", errorReader.readAllLines());
            inputReader.close();
            errorReader.close();

            int exitCode = process.exitValue();
            if (exitCode != 0 && expectSuccess) {
                throw new RuntimeException("Command '" + String.join(" ", command) + "' finished with exit code " + exitCode + ". Error output: " + stderr);
            } else if (exitCode == 0 && !expectSuccess) {
                var timestamp = formatter.format(initTimestamp);
                var unexpectedManifests = Exceptions.uncheck(() -> File.createTempFile("helm-test-unexpected-success-" + timestamp + "-", ".yaml"));
                Exceptions.uncheck(() -> Files.writeString(unexpectedManifests.toPath(), stdout));
                throw new RuntimeException("Command '" + String.join(" ", command) + "' unexpectedly finished with exit code 0. Manifests written to file '" + unexpectedManifests.getAbsolutePath() + "'");
            }

            return new StdProcessOutput(stdout, stderr);
        } catch (IOException | InterruptedException e) {
            try {
                if (inputReader != null) {
                    inputReader.close();
                }
            } catch (IOException ie) {
                e.addSuppressed(ie);
            }
            try {
                if (errorReader != null) {
                    errorReader.close();
                }
            } catch (IOException ie) {
                e.addSuppressed(ie);
            }
            throw new RuntimeException("Helm execution failed for command '" + String.join(" ", command) + "'", e);
        }
    }

    private record StdProcessOutput(String stdout, String stderr) {
    }
}
