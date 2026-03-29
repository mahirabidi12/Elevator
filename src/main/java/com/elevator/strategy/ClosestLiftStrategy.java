package com.elevator.strategy;

import java.util.List;

import com.elevator.model.Lift;
import com.elevator.request.OutsideCall;

public class ClosestLiftStrategy implements LiftSelectionStrategy {

    @Override
    public Lift selectLift(List<Lift> lifts, OutsideCall call) {
        Lift best = null;
        int minDistance = Integer.MAX_VALUE;

        for (Lift lift : lifts) {
            int distance = Math.abs(lift.getCurrentLevel() - call.getLevel());

            if (lift.isIdle()) {
                return lift;
            }

            if (distance < minDistance) {
                minDistance = distance;
                best = lift;
            }
        }
        return best;
    }
}
