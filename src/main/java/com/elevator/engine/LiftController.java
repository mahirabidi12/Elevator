package com.elevator.engine;

import java.util.*;

import com.elevator.model.Tower;
import com.elevator.model.Lift;
import com.elevator.model.Level;
import com.elevator.request.OutsideCall;
import com.elevator.strategy.LiftSelectionStrategy;

public class LiftController {

    private List<Lift> lifts;
    private LiftSelectionStrategy strategy;
    private final Tower tower;

    public LiftController(int numLifts, int totalLevels, LiftSelectionStrategy strategy, List<Integer> liftCapacities) {
        this.strategy = strategy;
        this.tower = new Tower(totalLevels);
        lifts = new ArrayList<>();

        for (int i = 0; i < numLifts; i++) {
            lifts.add(new Lift(i, totalLevels, liftCapacities.get(i), this));
        }
    }

    public void setStrategy(LiftSelectionStrategy strategy) {
        this.strategy = strategy;
    }

    public void handleOutsideCall(OutsideCall call) {
        Lift selected = strategy.selectLift(lifts, call);
        System.out.println("Assigning Lift " + selected.getId() + " to call at level " + call.getLevel());
        selected.addOutsideCall(call);
    }

    public void step() {
        for (Lift lift : lifts) {
            lift.step();
        }
    }

    public void handleHalt(Lift lift, int levelNumber) {
        Level level = tower.getLevel(levelNumber);

        lift.unload(100);

        int remainingCapacity = lift.getRemainingCapacity();
        int weightTaken = level.provideWeight(remainingCapacity);

        lift.addLoad(weightTaken);
    }

    public List<Lift> getLifts() {
        return lifts;
    }
}
