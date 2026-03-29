package com.elevator.request;

import com.elevator.model.Movement;

public class OutsideCall extends Call {
    Movement movement;

    public OutsideCall(int level, Movement movement) {
        this.level = level;
        this.movement = movement;
    }

    public Movement getMovement() {
        return movement;
    }

    public int getLevel() {
        return this.level;
    }
}
