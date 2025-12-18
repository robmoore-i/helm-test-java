package com.rrmoore.helm.test;

/**
 * Wraps the Helm executable in a usable interface for writing automated tests.
 */
public class HelmExecutor {

    /**
     * Creates a new Helm executor using the provided executable file.
     */
    public HelmExecutor() {
    }

    /**
     * Renders Helm templates using the provided Helm values.
     */
    public void template() {
        System.out.println("Foo");
    }
}
