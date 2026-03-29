package com.elevator.model;

public class Level {

    private final int levelNumber;
    private int pendingWeight;

    public Level(int levelNumber) {
        this.levelNumber = levelNumber;
    }

    public void addWeight(int weight) {
        pendingWeight += weight;
    }

    public int provideWeight(int allowedCapacity) {
        int weight = Math.min(pendingWeight, allowedCapacity);
        pendingWeight -= weight;
        return weight;
    }

    public int getLevelNumber() {
        return levelNumber;
    }
}
