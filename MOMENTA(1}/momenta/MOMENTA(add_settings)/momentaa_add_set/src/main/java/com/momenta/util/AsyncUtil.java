package com.momenta.util;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import javafx.concurrent.Task;

/**
 * Runs database / IO work on a background thread pool so the JavaFX
 * Application Thread never freezes. Success/failure callbacks are invoked
 * back on the FX thread automatically (javafx.concurrent.Task guarantees this).
 */
public class AsyncUtil {

    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "momenta-worker");
        t.setDaemon(true);
        return t;
    });

    public static <T> void run(Callable<T> backgroundWork, Consumer<T> onSuccess, Consumer<Throwable> onError) {
        Task<T> task = new Task<>() {
            @Override
            protected T call() throws Exception {
                return backgroundWork.call();
            }
        };
        task.setOnSucceeded(e -> {
            if (onSuccess != null) onSuccess.accept(task.getValue());
        });
        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            if (onError != null) onError.accept(ex);
            else if (ex != null) ex.printStackTrace();
        });
        EXECUTOR.submit(task);
    }

    /** Convenience overload for background work that returns nothing. */
    public static void runVoid(RunnableThrows backgroundWork, Runnable onSuccess, Consumer<Throwable> onError) {
        run(() -> {
            backgroundWork.run();
            return null;
        }, v -> {
            if (onSuccess != null) onSuccess.run();
        }, onError);
    }

    @FunctionalInterface
    public interface RunnableThrows {
        void run() throws Exception;
    }

    public static void shutdown() {
        EXECUTOR.shutdownNow();
    }
}
