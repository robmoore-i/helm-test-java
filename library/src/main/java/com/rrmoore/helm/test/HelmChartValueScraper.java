package com.rrmoore.helm.test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

public class HelmChartValueScraper {

    // Pattern for direct references such as .Values.foo.bar
    private static final Pattern DIRECT_VALUES_PATTERN = Pattern.compile("\\.Values\\.([a-zA-Z_][a-zA-Z0-9_]*(\\.[a-zA-Z_][a-zA-Z0-9_]*)*)");

    // Pattern for parenthesized references such as (.Values.foo).bar
    private static final Pattern PAREN_VALUES_PATTERN = Pattern.compile("\\(\\.Values\\.([a-zA-Z_][a-zA-Z0-9_]*)\\)\\.([a-zA-Z_][a-zA-Z0-9_]*)");

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
    public TreeSet<String> readValues(File chart) {
        var values = new TreeSet<String>();
        var templatesDir = new File(chart, "templates");

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

            // First, find parenthesized patterns and temporarily mark them to avoid double-matching
            var parenMatches = new LinkedHashSet<String>();
            var parenMatcher = PAREN_VALUES_PATTERN.matcher(content);
            while (parenMatcher.find()) {
                var base = parenMatcher.group(1);
                var suffix = parenMatcher.group(2);
                parenMatches.add(base + "." + suffix);
            }

            // Find direct patterns, but filter out partial matches that are
            // part of parenthesized patterns
            var directMatcher = DIRECT_VALUES_PATTERN.matcher(content);
            while (directMatcher.find()) {
                var value = directMatcher.group(1);
                // Check if this match starts with "(.Values" (parenthesized form)
                var start = directMatcher.start();
                if (start > 0 && content.charAt(start - 1) == '(') {
                    // This is a parenthesized form, skip
                    continue;
                }
                values.add(value);
            }

            // Add parenthesized matches
            values.addAll(parenMatches);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file: " + path, e);
        }
    }
}
