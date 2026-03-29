package com.elevator.strategy;

import java.util.List;

import com.elevator.model.Lift;
import com.elevator.request.OutsideCall;
import com.elevator.model.Movement;

public class MovementAwareStrategy implements LiftSelectionStrategy {

    @Override
    public Lift selectLift(List<Lift> lifts, OutsideCall call) {
        Lift best = null;
        int minDistance = Integer.MAX_VALUE;

        for (Lift lift : lifts) {

            if (isSuitable(lift, call)) {
                int distance = Math.abs(lift.getCurrentLevel() - call.getLevel());

                if (distance < minDistance) {
                    minDistance = distance;
                    best = lift;
                }
            }
        }

        if (best == null) {
            return new ClosestLiftStrategy().selectLift(lifts, call);
        }

        return best;
    }

    private boolean isSuitable(Lift lift, OutsideCall call) {
        if (lift.isIdle()) return true;

        if (lift.getMovement() == call.getMovement()) {
            if (call.getMovement() == Movement.UP &&
                lift.getCurrentLevel() <= call.getLevel()) {
                return true;
            }

            if (call.getMovement() == Movement.DOWN &&
                lift.getCurrentLevel() >= call.getLevel()) {
                return true;
            }
        }

        return false;
    }
}
