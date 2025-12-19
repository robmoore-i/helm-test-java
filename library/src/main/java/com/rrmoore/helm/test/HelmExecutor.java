package com.rrmoore.helm.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Wraps the Helm executable in a usable interface for writing automated tests.
 */
public class HelmExecutor {

    private final File helmExecutable;

    /**
     * Creates a Helm executor, determining the path to the Helm executable file using a JVM system property,
     * which is set automatically by applying the helm-test-java Gradle plugin.
     */
    public HelmExecutor() {
        this(new File(Objects.requireNonNull(
            System.getProperty("com.rrmoore.helm.test.executable.path"),
            "Missing system property for determining the path to the Helm executable downloaded by the helm-test-java Gradle plugin. " +
                "Is the plugin applied to this Gradle build?"
        )));
    }

    /**
     * Creates a Helm executor, using the provided Helm executable File i.e. the runnable `helm` binary file.
     */
    public HelmExecutor(File helmExecutable) {
        if (!helmExecutable.isFile()) {
            throw new IllegalArgumentException("Helm executable file '" + helmExecutable.getAbsolutePath() + "' does not exist.");
        } else if (!helmExecutable.canExecute() && !helmExecutable.setExecutable(true)) {
            throw new IllegalArgumentException("Helm executable file '" + helmExecutable.getAbsolutePath() + "' is not executable, and failed in an attempt to set it to be executable.");
        }
        this.helmExecutable = helmExecutable;
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
}
