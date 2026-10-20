package com.frankstiwrx.concurrencysim.scheduler;

import com.frankstiwrx.concurrencysim.model.Task;
import java.util.List;

public interface Scheduler {
    void schedule(List<Task> tasks, int numCores, int currentTime);
}