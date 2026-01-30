package com.rrmoore.helm.test.internal.jdkext;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.yaml.snakeyaml.Yaml;

/**
 * A wrapper for YAML objects, intended to reduce the number of casts needed to access deeply nested elements.
 */
public class YamlMap {

    private static final Yaml SNAKE_YAML = new Yaml();

    private final Map<String, Object> object;

    public YamlMap(String yaml) {
        this.object = SNAKE_YAML.load(yaml);
    }

    public YamlMap(Map<String, Object> object) {
        this.object = object;
    }

    public Object get(String key) {
        return object.get(key);
    }

    public String getString(String key) {
        return (String) object.get(key);
    }

    public Optional<Object> getNested(String dotSeparatedKeys) {
        if (dotSeparatedKeys.isBlank()) {
            return Optional.of(this);
        }
        var orderedKeys = dotSeparatedKeys.split("[.]");
        Object next = object;
        for (String orderedKey : orderedKeys) {
            if (next instanceof Map<?, ?> nextMap) {
                next = nextMap.get(orderedKey);
            } else {
                return Optional.empty();
            }
        }
        return Optional.ofNullable(next);
    }

    public Optional<String> getNestedString(String dotSeparatedKeys) {
        var next = getNested(dotSeparatedKeys);
        if (next.isPresent() && next.get() instanceof String) {
            return Optional.of((String) next.get());
        }
        return Optional.empty();
    }

    public Optional<YamlMap> getNestedObject(String dotSeparatedKeys) {
        var next = getNested(dotSeparatedKeys);
        if (next.isPresent() && next.get() instanceof Map<?, ?> nextMap) {
            var typeNormalizedMap = nextMap.entrySet().stream()
                .map(it -> Map.entry((String) it.getKey(), (Object) it.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            return Optional.of(new YamlMap(typeNormalizedMap));
        }
        return Optional.empty();
    }

    public Optional<List<Object>> getNestedList(String dotSeparatedKeys) {
        var next = getNested(dotSeparatedKeys);
        if (next.isPresent() && next.get() instanceof List<?> nextList) {
            return Optional.of(nextList.stream().map(it -> (Object) it).toList());
        }
        return Optional.empty();
    }
}
