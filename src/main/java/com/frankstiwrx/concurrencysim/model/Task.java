package com.frankstiwrx.concurrencysim.model;

public class Task {
    public enum State { READY, RUNNING, BLOCKED, FINISHED, MISSED }

    private final int id;
    private final String name;
    private int executionTime;
    private int remainingTime;
    private int deadline;
    private int priority;
    private State state;
    
    private long cpuCyclesConsumed;
    private long busyWaitingCycles;

    public Task(int id, String name, int executionTime, int deadline, int priority) {
        this.id = id;
        this.name = name;
        this.executionTime = executionTime;
        this.remainingTime = executionTime;
        this.deadline = deadline;
        this.priority = priority;
        this.state = State.READY;
    }

    public int getLaxity(int currentTime) {
        return deadline - currentTime - remainingTime;
    }

    public void tickExecution() {
        if (remainingTime > 0) {
            remainingTime--;
            cpuCyclesConsumed++;
            if (remainingTime == 0) {
                state = State.FINISHED;
            }
        }
    }

    public void tickBusyWait() {
        busyWaitingCycles++;
        cpuCyclesConsumed++;
    }

    public void reset(int currentTime) {
        this.remainingTime = executionTime;
        this.cpuCyclesConsumed = 0;
        this.busyWaitingCycles = 0;
        this.state = State.READY;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public int getExecutionTime() { return executionTime; }
    public int getRemainingTime() { return remainingTime; }
    public int getDeadline() { return deadline; }
    public int getPriority() { return priority; }
    public State getState() { return state; }
    public void setState(State state) { this.state = state; }
    public long getCpuCyclesConsumed() { return cpuCyclesConsumed; }
    public long getBusyWaitingCycles() { return busyWaitingCycles; }
}