package com.frankstiwrx.concurrencysim.sync;

import com.frankstiwrx.concurrencysim.model.Task;

public class MutexLock {
    private boolean locked = false;

    public synchronized void lock(Task task) throws InterruptedException {
        while (locked) {
            task.setState(Task.State.BLOCKED);
            wait();
        }
        locked = true;
        task.setState(Task.State.RUNNING);
    }

    public synchronized void unlock() {
        locked = false;
        notify();
    }
}