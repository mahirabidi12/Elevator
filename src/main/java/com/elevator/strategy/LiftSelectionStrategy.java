package com.elevator.strategy;

import java.util.List;

import com.elevator.model.Lift;
import com.elevator.request.OutsideCall;

public interface LiftSelectionStrategy {
    Lift selectLift(List<Lift> lifts, OutsideCall call);
}
