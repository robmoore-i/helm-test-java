package com.rrmoore.helm.test;

import java.io.File;

public class HelmChart {

    private final File file;

    /**
     * Creates a HelmChart from the given file or directory.
     *
     * @param file Either a directory containing the Helm chart, or a packaged Helm chart (i.e. a tarball).
     * @throws IllegalArgumentException if the file does not exist.
     */
    public HelmChart(File file) {
        if (!file.exists()) {
            throw new IllegalArgumentException("Helm chart '" + file.getAbsolutePath() + "' does not exist.");
        }
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    public File getTemplatesDir() {
        return new File(file, "templates");
    }

    public File getSchemaFile() {
        return new File(file, "values.schema.json");
    }
}