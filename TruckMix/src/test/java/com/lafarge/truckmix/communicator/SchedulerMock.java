package com.lafarge.truckmix.communicator;


public class SchedulerMock extends Scheduler {

    private Runnable task;

    @Override
    public void start(Runnable task) {
        this.task = task;
        flush();
    }

    @Override
    public void reset() {
        this.task = null;
    }

    public void flush() {
        this.flush(1);
    }

    public void flush(int n) {
        if (this.task != null) {
            for (int i = 0; i < n; i++) this.task.run();
        }
    }

    public boolean hasSchedule() {
        return this.task != null;
    }
}
