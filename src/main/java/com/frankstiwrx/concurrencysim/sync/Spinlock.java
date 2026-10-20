package com.frankstiwrx.concurrencysim.sync;

import com.frankstiwrx.concurrencysim.model.Task;
import java.util.concurrent.atomic.AtomicBoolean;

public class Spinlock {
    private final AtomicBoolean locked = new AtomicBoolean(false);

    public void lock(Task task) {
        while (!locked.compareAndSet(false, true)) {
            // Conta ciclo de CPU desperdiçado em busy waiting
            task.tickBusyWait();
            Thread.onSpinWait(); // Dica de baixo nível para a JVM/CPU
        }
    }

    public void unlock() {
        locked.set(false);
    }
}