package com.elevator.factory;

import java.util.List;

import com.elevator.engine.LiftController;
import com.elevator.strategy.*;

public class LiftControllerFactory {
    public static LiftController createLiftController(int numLifts, int totalLevels, String strategyType, List<Integer> liftCapacities) {
        LiftSelectionStrategy strategy;
        switch (strategyType) {
            case "CLOSEST":
                strategy = new ClosestLiftStrategy();
                break;
            case "MOVEMENT_AWARE":
                strategy = new MovementAwareStrategy();
                break;
            default:
                throw new IllegalArgumentException("Unknown strategy type");
        }
        return new LiftController(numLifts, totalLevels, strategy, liftCapacities);
    }
}
