package com.elevator;

import java.util.List;

import com.elevator.engine.LiftController;
import com.elevator.model.Tower;
import com.elevator.factory.LiftControllerFactory;

public class Main {

    public static void main(String[] args) throws InterruptedException {

        Tower tower = new Tower(10);
        LiftController controller = LiftControllerFactory.createLiftController(3, tower.getTotalLevels(), "CLOSEST", List.of(1000, 1000, 1000));

        Thread controllerThread = new Thread(() -> {
            while (true) {
                controller.step();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ignored) {}
            }
        });

        controllerThread.start();

        int numPassengers = 5;
        for (int i = 0; i < numPassengers; i++) {
            new Thread(new PassengerSimulator(controller, tower, i)).start();
        }
    }
}
