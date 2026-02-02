package com.rrmoore.helm.test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

public class HelmChartValueScraper {

    // Pattern for .Values.foo.bar references (applied after stripping parentheses)
    private static final Pattern VALUES_PATTERN = Pattern.compile("\\.Values\\.([a-zA-Z_][a-zA-Z0-9_]*(\\.[a-zA-Z_][a-zA-Z0-9_]*)*)");

    /**
     * Reads the templates in the given Helm chart directory and extracts a list of the referenced Helm values.
     * <p>
     * This does not do any proper parsing of the Go template, so there are some limitations:
     * - Variables are not followed. If you set {{ $x := .Values.foo }} and later use {{ $x.bar }}, this method will not detect the value 'foo.bar'.
     * - Method calls are not followed. If you run {{ include "myfunc" .Values.arg }} and myfunc uses {{ .subarg }}, this method will not detect the value 'arg.subarg'.
     * - Scoping is not considered. If you create a scope using {{ with .Values.top }} and in it use {{ .inner }}, this method will not detect the value 'top.inner'.
     * <p>
     * In essence, this method goes through the lines of Go template looking for the pattern .Values.*.
     */
    public TreeSet<String> readValues(HelmChart chart) {
        var values = new TreeSet<String>();
        var templatesDir = chart.getTemplatesDir();

        if (!templatesDir.isDirectory()) {
            return values;
        }

        try (var paths = Files.walk(templatesDir.toPath())) {
            paths.filter(Files::isRegularFile)
                 .filter(p -> {
                     var name = p.getFileName().toString();
                     return name.endsWith(".yaml") || name.endsWith(".yml") || name.endsWith(".tpl");
                 })
                 .forEach(path -> extractValuesFromFile(path, values));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read templates directory", e);
        }

        return values;
    }

    private void extractValuesFromFile(Path path, Set<String> values) {
        try {
            var content = Files.readString(path);

            // Strip all parentheses to handle nested forms like (((.Values.a).b).c).d
            var stripped = content.replace("(", "").replace(")", "");

            var matcher = VALUES_PATTERN.matcher(stripped);
            while (matcher.find()) {
                values.add(matcher.group(1));
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file: " + path, e);
        }
    }
}
