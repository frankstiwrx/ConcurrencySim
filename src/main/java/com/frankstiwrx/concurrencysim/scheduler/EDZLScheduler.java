package com.frankstiwrx.concurrencysim.scheduler;

import com.frankstiwrx.concurrencysim.model.Task;
import java.util.List;

public class EDZLScheduler implements Scheduler {

    @Override
    public void schedule(List<Task> tasks, int numCores, int currentTime) {
        if (tasks == null || tasks.isEmpty()) return;

        // 1. Marca estouro de prazo
        for (Task t : tasks) {
            if (t.getState() != Task.State.FINISHED) {
                if (t.getLaxity(currentTime) < 0 && t.getRemainingTime() > 0) {
                    t.setState(Task.State.MISSED);
                }
            }
        }

        // 2. Ordenação rigorosa e determinística (TimSort Safe)
        tasks.sort((t1, t2) -> {
            boolean active1 = (t1.getState() == Task.State.READY || t1.getState() == Task.State.RUNNING);
            boolean active2 = (t2.getState() == Task.State.READY || t2.getState() == Task.State.RUNNING);

            if (active1 != active2) {
                return active1 ? -1 : 1;
            }
            if (!active1) {
                return Integer.compare(t1.getId(), t2.getId());
            }

            // Regra 1 EDZL: Zero Laxity (Folga <= 0) tem prioridade absoluta
            int lax1 = t1.getLaxity(currentTime);
            int lax2 = t2.getLaxity(currentTime);

            boolean zeroLax1 = (lax1 <= 0 && t1.getRemainingTime() > 0);
            boolean zeroLax2 = (lax2 <= 0 && t2.getRemainingTime() > 0);

            if (zeroLax1 != zeroLax2) {
                return zeroLax1 ? -1 : 1;
            }

            // Regra 2 EDF: Menor Deadline Absoluto
            int deadCmp = Integer.compare(t1.getAbsoluteDeadline(), t2.getAbsoluteDeadline());
            if (deadCmp != 0) {
                return deadCmp;
            }

            return Integer.compare(t1.getId(), t2.getId());
        });
    }
}