package com.elevator.request;

public class InsideCall extends Call {

    public InsideCall(int level) {
        this.level = level;
    }

    public int getLevel() {
        return this.level;
    }
}
