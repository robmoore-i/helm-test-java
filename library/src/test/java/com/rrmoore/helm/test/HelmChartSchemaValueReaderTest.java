package com.rrmoore.helm.test;

import java.io.File;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HelmChartSchemaValueReaderTest {

    private final HelmChartSchemaValueReader reader = new HelmChartSchemaValueReader();

    @Test
    void readsValueFromSimpleSchema() {
        var values = reader.readValuesFromSchemaFile(new File("src/test/resources/values-schemas/simple-values.schema.json"));
        assertEquals(new TreeSet<>(Set.of(
            "image.pullPolicy",
            "replicas"
        )), values);
    }

    @Test
    void readsValueFromSchemaWithoutProperties() {
        var values = reader.readValuesFromSchemaFile(new File("src/test/resources/values-schemas/no-properties-values.schema.json"));
        assertEquals(Collections.emptySet(), values);
    }

    @Test
    void readsValueFromSchemaOfEmptyObject() {
        var values = reader.readValuesFromSchemaFile(new File("src/test/resources/values-schemas/empty-object-values.schema.json"));
        assertEquals(Collections.emptySet(), values);
    }

    @Test
    void readsValueFromSchemaOfEmptyFile() {
        var values = reader.readValuesFromSchemaFile(new File("src/test/resources/values-schemas/empty-file-values.schema.json"));
        assertEquals(Collections.emptySet(), values);
    }

    @Test
    void refusesToReadValuesFromNonExistentFile() {
        try {
            var directory = new File("src/test/resources/values-schemas");
            reader.readValuesFromSchemaFile(directory);
            throw new AssertionError("Expected an exception to be thrown.");
        } catch (Exception e) {
            // This is expected.
        }
    }

    @Test
    void readsValuesFromMyAppChart() {
        var values = reader.readValues(new HelmChart(new File("src/test/resources/my-app")));
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
