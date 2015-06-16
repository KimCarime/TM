package com.lafarge.truckmix.communicator;

public class CountingSchedulerMock extends Scheduler {

    private Runnable task;

    private long count = 0;

    public CountingSchedulerMock(long wait) {
        super(wait);
    }

    @Override
    public void start(Runnable task) {
        this.task = task;
        this.task.run();
    }

    @Override
    public void reset() {
        this.task = null;
        this.count = 0;
    }

    public void forward(long time) {
        if (this.task != null) {
            this.count += time;
            if (this.count >= this.wait) {
                this.task.run();
                this.count = this.count - this.wait;
            }
        }
    }
}
