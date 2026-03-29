package com.elevator.model;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

import com.elevator.engine.LiftController;
import com.elevator.request.OutsideCall;
import com.elevator.request.InsideCall;

public class Lift implements Runnable {
    private final int id;
    private final int maxLevel;
    private final int maxCapacity;

    private int currentLevel;
    private int currentLoad;

    private Movement movement;
    private LiftState state;

    private final PriorityQueue<Integer> upQueue;
    private final PriorityQueue<Integer> downQueue;

    private final ReentrantLock lock = new ReentrantLock();

    private final LiftController controller;

    public Lift(int id, int maxLevel, int maxCapacity, LiftController controller) {
        this.id = id;
        this.maxLevel = maxLevel;
        this.maxCapacity = maxCapacity;
        this.controller = controller;

        this.currentLevel = 0;
        this.currentLoad = 0;

        this.movement = Movement.IDLE;
        this.state = LiftState.IDLE;

        this.upQueue = new PriorityQueue<>();
        this.downQueue = new PriorityQueue<>(Collections.reverseOrder());
    }

    public int getId() {
        return id;
    }

    public void addInsideCall(InsideCall call) {
        lock.lock();
        try {
            if (call.getLevel() > currentLevel) {
                upQueue.offer(call.getLevel());
            } else {
                downQueue.offer(call.getLevel());
            }
            if (state == LiftState.IDLE) {
                state = LiftState.MOVING;
                pickMovement();
            }
        } finally {
            lock.unlock();
        }
    }

    public void addOutsideCall(OutsideCall call) {
        lock.lock();
        try {
            int level = call.getLevel();

            if (level > currentLevel) {
                upQueue.offer(level);
            } else {
                downQueue.offer(level);
            }

            if (state == LiftState.IDLE) {
                state = LiftState.MOVING;
                pickMovement();
            }

        } finally {
            lock.unlock();
        }
    }

    public int getCurrentLevel() {
        lock.lock();
        try {
            return currentLevel;
        } finally {
            lock.unlock();
        }
    }

    public Movement getMovement() {
        lock.lock();
        try {
            return movement;
        } finally {
            lock.unlock();
        }
    }

    public boolean isIdle() {
        lock.lock();
        try {
            return state == LiftState.IDLE;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void run() {
        while (true) {
            step();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {}
        }
    }

    public void step() {
        lock.lock();
        try {

            if (isOverloaded()) {
                state = LiftState.STOPPED;
                movement = Movement.IDLE;
                System.out.println("Lift " + id + " overloaded");
                return;
            }

            if (upQueue.isEmpty() && downQueue.isEmpty()) {
                movement = Movement.IDLE;
                state = LiftState.IDLE;
                return;
            }

            if (movement == Movement.IDLE) {
                pickMovement();
            }

            if (movement == Movement.UP) {
                ascend();
            } else if (movement == Movement.DOWN) {
                descend();
            }

        } finally {
            lock.unlock();
        }
    }

    private void ascend() {
        if (!upQueue.isEmpty() && currentLevel < maxLevel) {
            currentLevel++;

            if (currentLevel == upQueue.peek()) {
                upQueue.poll();
                halt();
            }
        } else {
            if (downQueue.isEmpty()) {
                movement = Movement.IDLE;
                state = LiftState.IDLE;
            } else {
                movement = Movement.DOWN;
            }
        }
    }

    private void descend() {
        if (!downQueue.isEmpty() && currentLevel > 0) {
            currentLevel--;

            if (currentLevel == downQueue.peek()) {
                downQueue.poll();
                halt();
            }
        } else {
            if (upQueue.isEmpty()) {
                movement = Movement.IDLE;
                state = LiftState.IDLE;
            } else {
                movement = Movement.UP;
            }
        }
    }

    private void pickMovement() {
        if (!upQueue.isEmpty()) {
            movement = Movement.UP;
        } else if (!downQueue.isEmpty()) {
            movement = Movement.DOWN;
        } else {
            movement = Movement.IDLE;
            state = LiftState.IDLE;
        }
    }

    private void halt() {
        state = LiftState.STOPPED;

        openDoor();
        controller.handleHalt(this, currentLevel);
        closeDoor();

        state = LiftState.MOVING;
    }

    public void addLoad(int weight) {
        currentLoad += weight;
    }

    public void unload(int weight) {
        currentLoad = Math.max(0, currentLoad - weight);
    }

    public int getRemainingCapacity() {
        return maxCapacity - currentLoad;
    }

    private boolean isOverloaded() {
        return currentLoad > maxCapacity;
    }

    private void openDoor() {
        System.out.println("Lift " + id + " opening at level " + currentLevel);
    }

    private void closeDoor() {
        System.out.println("Lift " + id + " closing");
    }
}
