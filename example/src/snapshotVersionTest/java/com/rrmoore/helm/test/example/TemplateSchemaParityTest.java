package com.rrmoore.helm.test.example;

import com.rrmoore.helm.test.HelmChart;
import java.io.File;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TemplateSchemaParityTest {

    @Test
    void allValuesInTemplatesAreIncludedInSchema() {
        var chart = new HelmChart(new File("src/main/helm/gym-register"));
        assertEquals(chart.readValuesFromSchema(), chart.readValuesFromTemplates());
    }
}
