package com.rrmoore.helm.test;

import com.networknt.schema.CollectorContext;
import com.networknt.schema.Error;
import com.networknt.schema.InputFormat;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.SpecificationVersion;
import com.networknt.schema.walk.PropertyWalkHandler;
import com.networknt.schema.walk.WalkEvent;
import com.networknt.schema.walk.WalkFlow;
import com.networknt.schema.walk.WalkListener;
import com.rrmoore.helm.test.internal.jdkext.Exceptions;
import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class HelmChartSchemaValueReader {

    public TreeSet<String> readValues(HelmChart chart) {
        return readValuesFromSchemaFile(chart.getSchemaFile());
    }

    public TreeSet<String> readValuesFromSchemaFile(File schemaFile) {
        if (!schemaFile.isFile()) {
            throw new RuntimeException("Cannot read values from non-existent schema file '" + schemaFile.getAbsolutePath() + "'. Create it and try again.");
        }
        var schema = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_7)
            .getSchema(Exceptions.uncheck(() -> new FileInputStream(schemaFile)));
        var walkHandler = PropertyWalkHandler.builder()
            .propertyWalkListener(new WalkListener() {
                @Override
                public WalkFlow onWalkStart(WalkEvent walkEvent) {
                    var key = walkEvent.getInstanceLocation().toString().substring(1).replaceAll("/", ".");
                    var collectorContext = walkEvent.getExecutionContext().getCollectorContext();
                    collectorContext.put(key, 0);
                    int lastDot = key.lastIndexOf(".");
                    if (lastDot != -1) {
                        var parentKey = key.substring(0, lastDot);
                        collectorContext.getData().remove(parentKey);
                    }
                    return WalkFlow.CONTINUE;
                }

                @Override
                public void onWalkEnd(WalkEvent walkEvent, List<Error> errors) {
                }
            })
            .build();

        var walkResult = schema.walk("{}", InputFormat.JSON, false,
            executionContext -> {
                executionContext.setCollectorContext(new CollectorContext(new ConcurrentHashMap<>()));
                executionContext.walkConfig(walkConfig -> walkConfig.propertyWalkHandler(walkHandler));
            });

        if (!walkResult.getErrors().isEmpty()) {
            throw new RuntimeException("Errors while traversing values.schema.json:\n" + walkResult.getErrors().stream().map(Error::getMessage).collect(Collectors.joining("\n")));
        }
        return walkResult.getCollectorContext().getData()
            .keySet().stream()
            .map(it -> (String) it)
            .collect(Collectors.toCollection(TreeSet::new));
    }
}
