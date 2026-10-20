package com.frankstiwrx.concurrencysim.model;

public class Task {
    public enum State { READY, RUNNING, BLOCKED, FINISHED, MISSED }

    private final int id;
    private final String name;
    private final int executionTime;
    private int remainingTime;
    private final int relativeDeadline;
    private int absoluteDeadline;
    private int priority;
    private State state;
    
    private long cpuCyclesConsumed;
    private long busyWaitingCycles;

    public Task(int id, String name, int executionTime, int relativeDeadline, int priority) {
        this.id = id;
        this.name = name;
        this.executionTime = executionTime;
        this.remainingTime = executionTime;
        this.relativeDeadline = relativeDeadline;
        this.absoluteDeadline = relativeDeadline;
        this.priority = priority;
        this.state = State.READY;
    }

    public int getLaxity(int currentTime) {
        return absoluteDeadline - currentTime - remainingTime;
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

    public void reset(int newCurrentTime) {
        this.remainingTime = executionTime;
        this.absoluteDeadline = newCurrentTime + relativeDeadline;
        this.cpuCyclesConsumed = 0;
        this.busyWaitingCycles = 0;
        this.state = State.READY;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public int getExecutionTime() { return executionTime; }
    public int getRemainingTime() { return remainingTime; }
    public int getRelativeDeadline() { return relativeDeadline; }
    public int getAbsoluteDeadline() { return absoluteDeadline; }
    public void setAbsoluteDeadline(int absoluteDeadline) { this.absoluteDeadline = absoluteDeadline; }
    public int getPriority() { return priority; }
    public State getState() { return state; }
    public void setState(State state) { this.state = state; }
    public long getCpuCyclesConsumed() { return cpuCyclesConsumed; }
    public long getBusyWaitingCycles() { return busyWaitingCycles; }
}