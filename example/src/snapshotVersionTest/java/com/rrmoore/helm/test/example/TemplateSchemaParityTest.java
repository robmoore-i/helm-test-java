package com.rrmoore.helm.test.example;

import com.rrmoore.helm.test.HelmChartSchemaValueReader;
import com.rrmoore.helm.test.HelmChartValueScraper;
import java.io.File;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TemplateSchemaParityTest {

    @Test
    void allValuesInTemplatesAreIncludedInSchema() {
        var chart = new File("src/main/helm/gym-register");
        var templateValues = new HelmChartValueScraper().readValues(chart);
        var schemaValues = new HelmChartSchemaValueReader().readValues(chart);
        assertEquals(schemaValues, templateValues);
    }
}
