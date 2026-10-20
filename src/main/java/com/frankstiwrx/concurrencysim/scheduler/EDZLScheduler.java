package com.frankstiwrx.concurrencysim.scheduler;

import com.frankstiwrx.concurrencysim.model.Task;
import java.util.List;

public class EDZLScheduler implements Scheduler {

    @Override
    public void schedule(List<Task> readyTasks, int numCores, int currentTime) {
        if (readyTasks.isEmpty()) return;

        readyTasks.sort((t1, t2) -> {
            int lax1 = t1.getLaxity(currentTime);
            int lax2 = t2.getLaxity(currentTime);

            boolean zeroLax1 = (lax1 <= 0 && t1.getRemainingTime() > 0);
            boolean zeroLax2 = (lax2 <= 0 && t2.getRemainingTime() > 0);

            if (zeroLax1 && !zeroLax2) return -1;
            if (!zeroLax1 && zeroLax2) return 1;

            return Integer.compare(t1.getDeadline(), t2.getDeadline());
        });
    }
}