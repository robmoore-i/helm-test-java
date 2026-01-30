package com.rrmoore.helm.test.internal.jdkext;

import java.util.function.Supplier;

public final class Exceptions {

    private Exceptions() {
    }

    public static void uncheck(ThrowingRunnable runnable, Supplier<String> errorMessageSupplier) {
        try {
            runnable.run();
        } catch (Exception e) {
            if (errorMessageSupplier != null) {
                throw new RuntimeException(errorMessageSupplier.get(), e);
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    public static <T> T uncheck(ThrowingSupplier<T> supplier, Supplier<String> errorMessageSupplier) {
        try {
            return supplier.get();
        } catch (Exception e) {
            if (errorMessageSupplier != null) {
                throw new RuntimeException(errorMessageSupplier.get(), e);
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    public static <T> T uncheck(ThrowingSupplier<T> supplier) {
        return uncheck(supplier, null);
    }

    public static void uncheck(ThrowingRunnable runnable) {
        uncheck(runnable, null);
    }

    public interface ThrowingSupplier<T> {

        T get() throws Exception;
    }

    public interface ThrowingRunnable {

        void run() throws Exception;
    }
}
