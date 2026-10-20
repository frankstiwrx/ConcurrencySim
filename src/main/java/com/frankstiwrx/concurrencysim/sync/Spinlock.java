package com.frankstiwrx.concurrencysim.sync;

import com.frankstiwrx.concurrencysim.model.Task;
import java.util.concurrent.atomic.AtomicBoolean;

public class Spinlock {
    private final AtomicBoolean locked = new AtomicBoolean(false);

    public void lock(Task task) {
        while (!locked.compareAndSet(false, true)) {
            task.tickBusyWait();
            Thread.onSpinWait();
        }
    }

    public void unlock() {
        locked.set(false);
    }
}