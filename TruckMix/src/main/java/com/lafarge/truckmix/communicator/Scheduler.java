package com.lafarge.truckmix.communicator;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Scheduler {

    private final ScheduledExecutorService ex;
    private final long wait;
    private ScheduledFuture<?> future;

    public Scheduler() {
        this(10 * 1000);
    }

    public Scheduler(long wait) {
        this.ex = Executors.newSingleThreadScheduledExecutor();
        this.wait = wait;
    }

    public void start(Runnable task) {
        this.reset();
        task.run();
        this.future = ex.scheduleAtFixedRate(task, wait, wait, TimeUnit.MILLISECONDS);
    }

    public void reset() {
        if (future != null && !future.isCancelled() && !future.isDone()) {
            future.cancel(false);
        }
        future = null;
    }
}
