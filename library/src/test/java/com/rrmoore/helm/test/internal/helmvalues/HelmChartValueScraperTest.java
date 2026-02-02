package com.rrmoore.helm.test.internal.helmvalues;

import com.rrmoore.helm.test.HelmChart;
import java.io.File;
import java.util.Set;
import java.util.TreeSet;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HelmChartValueScraperTest {

    @Test
    void scrapesValuesFromTemplates() {
        var scraper = new HelmChartValueScraper();
        var values = scraper.readValues(new HelmChart(new File("src/test/resources/my-app")));
        assertEquals(new TreeSet<>(Set.of(
            "checksumAnnotationTest.missingConfigMapVolumeAnnotation",
            "checksumAnnotationTest.missingEnvConfigMapAnnotation",
            "checksumAnnotationTest.missingEnvFromConfigMapAnnotation",
            "checksumAnnotationTest.missingEnvFromSecretAnnotation",
            "checksumAnnotationTest.missingEnvSecretAnnotation",
            "checksumAnnotationTest.missingImagePullSecretAnnotation",
            "checksumAnnotationTest.missingSecretVolumeAnnotation",
            "checksumAnnotationTest.noAnnotations",
            "checksumAnnotationTest.unnecessaryExtraResourceAnnotation",
            "config2.enabled",
            "deeply.nested.value.here",
            "edge.first",
            "edge.second",
            "edge.useFeature",
            "equalityTesting.useRandomSecret",
            "image.pullPolicy",
            "replicas",
            "with_underscore"
        )), values);
    }
}