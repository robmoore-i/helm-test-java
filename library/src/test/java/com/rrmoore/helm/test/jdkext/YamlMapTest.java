package com.rrmoore.helm.test.jdkext;

import java.util.Optional;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class YamlMapTest {

    private final YamlMap unit = new YamlMap("""
        apiVersion: apps/v1
        kind: Deployment
        metadata:
          name: gym-register-app
          labels:
            app.kubernetes.io/name: gym-register-app
        spec:
          replicas: 1
          selector:
            matchLabels:
              app.kubernetes.io/name: gym-register-app
          template:
            metadata:
              labels:
                appName: gym-register-app
              annotations:
                configChecksum: aodgiub82bgew
            spec:
              serviceAccountName: gym-register-app-svc-acc
              containers:
                - name: main
                  image: "gym-register-app"
                  imagePullPolicy: "IfNotPresent"
                  env:
                  - name: GYM_REGISTER_APP__ALLOW_WALK_INS
                    valueFrom:
                      configMapKeyRef:
                        name: gym-register-app-config
                        key: allowWalkIns
                  ports:
                    - name: http
                      containerPort: 80
                      protocol: TCP
                  livenessProbe:
                    httpGet:
                      path: /
                      port: http
                  readinessProbe:
                    httpGet:
                      path: /
                      port: http
        """);

    @Test
    void get() {
        assertEquals("Deployment", unit.get("kind"));
    }

    @Test
    void getString() {
        assertEquals("Deployment", unit.getString("kind"));
    }

    @Test
    void getNestedTopLevel() {
        assertEquals("Deployment", unit.getNested("kind").orElseThrow());
    }

    @Test
    void getNestedNonExisting() {
        assertEquals(Optional.empty(), unit.getNested("spec.foo"));
    }

    @Test
    void getNestedEmpty() {
        assertEquals(unit, unit.getNested("").orElseThrow());
    }

    @Test
    void getNestedString() {
        assertEquals("gym-register-app-svc-acc", unit.getNestedString("spec.template.spec.serviceAccountName").orElseThrow());
    }

    @Test
    void getNestedList() {
        assertEquals(1, unit.getNestedList("spec.template.spec.containers").orElseThrow().size());
    }

    @Test
    void getNestedObject() {
        var templateMetadata = unit.getNestedObject("spec.template.metadata").orElseThrow();
        assertEquals("gym-register-app", templateMetadata.getNestedString("labels.appName").orElseThrow());
    }
}
