package com.rrmoore.gradle.helm.test;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class HelmTestJavaPlugin implements Plugin<Project> {

    @Override
    public void apply(Project target) {
        target.getTasks().register("fooTask").configure(t -> {
            t.doFirst(innerT -> {
                target.getLogger().lifecycle("Foo!");
            });
        });
    }
}
