package com.elevator;

import java.util.Random;

import com.elevator.model.*;
import com.elevator.engine.LiftController;
import com.elevator.request.OutsideCall;

public class PassengerSimulator implements Runnable {

    private final LiftController controller;
    private final Tower tower;
    private final int passengerId;
    private final Random random = new Random();

    public PassengerSimulator(LiftController controller, Tower tower, int passengerId) {
        this.controller = controller;
        this.tower = tower;
        this.passengerId = passengerId;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(random.nextInt(3000) + 1000);
        } catch (InterruptedException ignored) {}

        int source = random.nextInt(tower.getTotalLevels());
        int destination;

        do {
            destination = random.nextInt(tower.getTotalLevels());
        } while (destination == source);

        int weight = random.nextInt(70) + 40;

        Movement movement = (destination > source)
                ? Movement.UP
                : Movement.DOWN;

        System.out.println("Passenger " + passengerId +
                " at level " + source +
                " wants to go to " + destination +
                " (weight=" + weight + ")");

        Level level = tower.getLevel(source);
        level.addWeight(weight);

        controller.handleOutsideCall(new OutsideCall(source, movement));
    }
}
