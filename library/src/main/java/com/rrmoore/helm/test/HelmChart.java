package com.rrmoore.helm.test;

import com.rrmoore.helm.test.internal.helmvalues.HelmChartSchemaValueReader;
import com.rrmoore.helm.test.internal.helmvalues.HelmChartValueScraper;
import java.io.File;
import java.util.TreeSet;

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

    /**
     * @return A HelmExecutor for executing `helm` in the context of this chart.
     */
    public HelmExecutor getHelmExecutor() {
        return new HelmExecutor(this);
    }

    /**
     * @return A HelmExecutor for executing `helm` in the context of this chart, using a directly specified Helm executable.
     */
    public HelmExecutor getHelmExecutor(File helmExecutable) {
        return new HelmExecutor(helmExecutable, this);
    }

    /**
     * Reads the files under the template/ directory of the Helm chart to deduce the set of values that can be used with it.
     */
    public TreeSet<String> readValuesFromTemplates() {
        return new HelmChartValueScraper().readValues(this);
    }

    /**
     * Reads the values.schema.json file and returns the set of (leaf i.e. non-parent) values that it includes.
     */
    public TreeSet<String> readValuesFromSchema() {
        return new HelmChartSchemaValueReader().readValues(this);
    }
}