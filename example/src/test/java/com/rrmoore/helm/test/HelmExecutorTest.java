package com.rrmoore.helm.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.time.Duration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HelmExecutorTest {

    @Test
    void canGetVersion() throws IOException, InterruptedException {
        var helmPath = System.getProperty("com.rrmoore.helm.test.executable.path");

        var process = new ProcessBuilder(helmPath, "version").start();
        process.waitFor(Duration.ofSeconds(1));
        try (BufferedReader bufferedReader = process.inputReader()) {
            var output = String.join("\n", bufferedReader.readAllLines());
            assertEquals("version.BuildInfo{Version:\"v3.19.4\", GitCommit:\"7cfb6e486dac026202556836bb910c37d847793e\", GitTreeState:\"clean\", GoVersion:\"go1.24.11\"}", output);
        }
        assertEquals(0, process.exitValue());
    }
}
