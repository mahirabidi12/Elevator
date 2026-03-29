package com.elevator;

import java.util.*;

import com.elevator.model.*;
import com.elevator.engine.LiftController;
import com.elevator.factory.LiftControllerFactory;
import com.elevator.request.*;
import com.elevator.strategy.*;

public class ElevatorSystemTest {

    static int passed = 0;
    static int failed = 0;

    public static void main(String[] args) {

        // ── Model Tests ──
        testLevel();
        testTower();
        testMovementEnum();
        testLiftStateEnum();

        // ── Request Tests ──
        testOutsideCall();
        testInsideCall();

        // ── Strategy Tests ──
        testClosestLiftStrategy();
        testMovementAwareStrategy();

        // ── Factory Tests ──
        testLiftControllerFactory();
        testFactoryInvalidStrategy();

        // ── Lift Tests ──
        testLiftInitialState();
        testLiftOutsideCallMovesUp();
        testLiftOutsideCallMovesDown();
        testLiftDirectionSwitch();
        testLiftLoadUnload();
        testLiftOverload();

        // ── LiftController Tests ──
        testControllerHandleOutsideCall();
        testControllerStep();
        testControllerHandleHalt();

        // ── Integration Test ──
        testFullSimulation();

        System.out.println("\n========================================");
        System.out.println("Results: " + passed + " passed, " + failed + " failed out of " + (passed + failed));
        System.out.println("========================================");

        if (failed > 0) {
            System.exit(1);
        }
    }

    // ─────────── Model Tests ───────────

    static void testLevel() {
        Level level = new Level(5);
        assertEqual("Level number", 5, level.getLevelNumber());

        level.addWeight(80);
        level.addWeight(60);
        // Total pending = 140, ask for 100 => get 100, remaining = 40
        int taken = level.provideWeight(100);
        assertEqual("Level provideWeight partial", 100, taken);

        // Remaining 40, ask for 200 => get 40
        int rest = level.provideWeight(200);
        assertEqual("Level provideWeight remaining", 40, rest);

        // Nothing left
        int empty = level.provideWeight(100);
        assertEqual("Level provideWeight empty", 0, empty);

        pass("testLevel");
    }

    static void testTower() {
        Tower tower = new Tower(10);
        assertEqual("Tower totalLevels", 10, tower.getTotalLevels());
        assertEqual("Tower level 0", 0, tower.getLevel(0).getLevelNumber());
        assertEqual("Tower level 9", 9, tower.getLevel(9).getLevelNumber());
        pass("testTower");
    }

    static void testMovementEnum() {
        assertEqual("Movement UP", "UP", Movement.UP.name());
        assertEqual("Movement DOWN", "DOWN", Movement.DOWN.name());
        assertEqual("Movement IDLE", "IDLE", Movement.IDLE.name());
        assertEqual("Movement values count", 3, Movement.values().length);
        pass("testMovementEnum");
    }

    static void testLiftStateEnum() {
        assertEqual("LiftState MOVING", "MOVING", LiftState.MOVING.name());
        assertEqual("LiftState STOPPED", "STOPPED", LiftState.STOPPED.name());
        assertEqual("LiftState IDLE", "IDLE", LiftState.IDLE.name());
        assertEqual("LiftState values count", 3, LiftState.values().length);
        pass("testLiftStateEnum");
    }

    // ─────────── Request Tests ───────────

    static void testOutsideCall() {
        OutsideCall call = new OutsideCall(3, Movement.UP);
        assertEqual("OutsideCall level", 3, call.getLevel());
        assertEqual("OutsideCall movement", Movement.UP, call.getMovement());
        pass("testOutsideCall");
    }

    static void testInsideCall() {
        InsideCall call = new InsideCall(7);
        assertEqual("InsideCall level", 7, call.getLevel());
        pass("testInsideCall");
    }

    // ─────────── Strategy Tests ───────────

    static void testClosestLiftStrategy() {
        LiftController controller = new LiftController(3, 10, new ClosestLiftStrategy(), List.of(1000, 1000, 1000));
        List<Lift> lifts = controller.getLifts();

        // All idle — should return the first idle one (id=0)
        OutsideCall call = new OutsideCall(5, Movement.UP);
        ClosestLiftStrategy strategy = new ClosestLiftStrategy();
        Lift selected = strategy.selectLift(lifts, call);
        assertEqual("ClosestLift picks first idle", 0, selected.getId());

        // Move lift 0 so it's no longer idle, then lift 1 should be picked (idle)
        lifts.get(0).addOutsideCall(new OutsideCall(8, Movement.UP));
        lifts.get(0).step(); // now lift 0 is MOVING
        selected = strategy.selectLift(lifts, call);
        assertEqual("ClosestLift picks next idle", 1, selected.getId());

        pass("testClosestLiftStrategy");
    }

    static void testMovementAwareStrategy() {
        LiftController controller = new LiftController(2, 10, new MovementAwareStrategy(), List.of(1000, 1000));
        List<Lift> lifts = controller.getLifts();

        // Both idle — should pick first idle
        MovementAwareStrategy strategy = new MovementAwareStrategy();
        OutsideCall call = new OutsideCall(5, Movement.UP);
        Lift selected = strategy.selectLift(lifts, call);
        assertEqual("MovementAware picks idle", 0, selected.getId());

        pass("testMovementAwareStrategy");
    }

    // ─────────── Factory Tests ───────────

    static void testLiftControllerFactory() {
        LiftController c1 = LiftControllerFactory.createLiftController(2, 10, "CLOSEST", List.of(500, 500));
        assertNotNull("Factory CLOSEST", c1);
        assertEqual("Factory CLOSEST lift count", 2, c1.getLifts().size());

        LiftController c2 = LiftControllerFactory.createLiftController(4, 20, "MOVEMENT_AWARE", List.of(800, 800, 800, 800));
        assertNotNull("Factory MOVEMENT_AWARE", c2);
        assertEqual("Factory MOVEMENT_AWARE lift count", 4, c2.getLifts().size());

        pass("testLiftControllerFactory");
    }

    static void testFactoryInvalidStrategy() {
        try {
            LiftControllerFactory.createLiftController(1, 5, "INVALID", List.of(100));
            fail("testFactoryInvalidStrategy", "Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            pass("testFactoryInvalidStrategy");
        }
    }

    // ─────────── Lift Tests ───────────

    static void testLiftInitialState() {
        LiftController controller = new LiftController(1, 10, new ClosestLiftStrategy(), List.of(1000));
        Lift lift = controller.getLifts().get(0);

        assertEqual("Lift initial level", 0, lift.getCurrentLevel());
        assertEqual("Lift initial movement", Movement.IDLE, lift.getMovement());
        assertTrue("Lift initial idle", lift.isIdle());
        assertEqual("Lift initial capacity", 1000, lift.getRemainingCapacity());

        pass("testLiftInitialState");
    }

    static void testLiftOutsideCallMovesUp() {
        LiftController controller = new LiftController(1, 10, new ClosestLiftStrategy(), List.of(1000));
        Lift lift = controller.getLifts().get(0);

        lift.addOutsideCall(new OutsideCall(3, Movement.UP));
        assertFalse("Lift not idle after call", lift.isIdle());

        // Step 3 times to reach level 3
        lift.step(); // level 1
        lift.step(); // level 2
        lift.step(); // level 3 — stops here

        assertEqual("Lift reached level 3", 3, lift.getCurrentLevel());

        // One more step — queues empty, should go idle
        lift.step();
        assertTrue("Lift idle after completing", lift.isIdle());

        pass("testLiftOutsideCallMovesUp");
    }

    static void testLiftOutsideCallMovesDown() {
        LiftController controller = new LiftController(1, 10, new ClosestLiftStrategy(), List.of(1000));
        Lift lift = controller.getLifts().get(0);

        // First move lift up to level 5
        lift.addOutsideCall(new OutsideCall(5, Movement.UP));
        for (int i = 0; i < 5; i++) lift.step();
        assertEqual("Lift at level 5", 5, lift.getCurrentLevel());
        lift.step(); // go idle

        // Now request to go down to level 2
        lift.addOutsideCall(new OutsideCall(2, Movement.DOWN));
        lift.step(); // level 4
        lift.step(); // level 3
        lift.step(); // level 2 — stops

        assertEqual("Lift reached level 2", 2, lift.getCurrentLevel());

        pass("testLiftOutsideCallMovesDown");
    }

    static void testLiftDirectionSwitch() {
        LiftController controller = new LiftController(1, 10, new ClosestLiftStrategy(), List.of(1000));
        Lift lift = controller.getLifts().get(0);

        // Add up request and down request
        lift.addOutsideCall(new OutsideCall(3, Movement.UP));
        lift.addOutsideCall(new OutsideCall(1, Movement.UP)); // 1 > 0 so goes to upQueue

        // Step to 1 (stop), then to 3 (stop)
        lift.step(); // level 1 — stop
        assertEqual("Direction switch: at level 1", 1, lift.getCurrentLevel());

        lift.step(); // level 2
        lift.step(); // level 3 — stop
        assertEqual("Direction switch: at level 3", 3, lift.getCurrentLevel());

        lift.step(); // idle
        assertTrue("Direction switch: idle after both", lift.isIdle());

        pass("testLiftDirectionSwitch");
    }

    static void testLiftLoadUnload() {
        LiftController controller = new LiftController(1, 10, new ClosestLiftStrategy(), List.of(500));
        Lift lift = controller.getLifts().get(0);

        assertEqual("Initial capacity", 500, lift.getRemainingCapacity());

        lift.addLoad(200);
        assertEqual("After load 200", 300, lift.getRemainingCapacity());

        lift.addLoad(100);
        assertEqual("After load 100 more", 200, lift.getRemainingCapacity());

        lift.unload(150);
        assertEqual("After unload 150", 350, lift.getRemainingCapacity());

        // Unload more than current — should clamp to 0 load
        lift.unload(9999);
        assertEqual("After over-unload", 500, lift.getRemainingCapacity());

        pass("testLiftLoadUnload");
    }

    static void testLiftOverload() {
        LiftController controller = new LiftController(1, 10, new ClosestLiftStrategy(), List.of(100));
        Lift lift = controller.getLifts().get(0);

        lift.addOutsideCall(new OutsideCall(5, Movement.UP));
        lift.addLoad(200); // exceeds capacity of 100

        int levelBefore = lift.getCurrentLevel();
        lift.step(); // should detect overload and not move
        assertEqual("Overloaded lift stays put", levelBefore, lift.getCurrentLevel());

        pass("testLiftOverload");
    }

    // ─────────── LiftController Tests ───────────

    static void testControllerHandleOutsideCall() {
        LiftController controller = new LiftController(3, 10, new ClosestLiftStrategy(), List.of(1000, 1000, 1000));

        // All idle — first idle lift (0) should be assigned
        controller.handleOutsideCall(new OutsideCall(5, Movement.UP));

        Lift lift0 = controller.getLifts().get(0);
        assertFalse("Controller assigned lift 0", lift0.isIdle());

        pass("testControllerHandleOutsideCall");
    }

    static void testControllerStep() {
        LiftController controller = new LiftController(2, 10, new ClosestLiftStrategy(), List.of(1000, 1000));
        controller.handleOutsideCall(new OutsideCall(2, Movement.UP));

        controller.step();
        Lift lift0 = controller.getLifts().get(0);
        assertEqual("Controller step moves lift", 1, lift0.getCurrentLevel());

        controller.step();
        assertEqual("Controller second step", 2, lift0.getCurrentLevel());

        pass("testControllerStep");
    }

    static void testControllerHandleHalt() {
        LiftController controller = new LiftController(1, 10, new ClosestLiftStrategy(), List.of(1000));
        Lift lift = controller.getLifts().get(0);

        // Simulate: add weight to level 2, send lift there
        Tower tower = new Tower(10);
        Level level2 = tower.getLevel(2);
        level2.addWeight(75);

        // Manually test handleHalt via controller's own tower
        // The controller has its own tower, so let's use the full flow
        // Add weight to controller's internal tower by going through the outside call flow
        controller.handleOutsideCall(new OutsideCall(1, Movement.UP));
        controller.step(); // lift moves to 1, stops (handleHalt is called internally)

        // After halt, lift should have unloaded 100 (clamped to 0) and loaded from level
        // Since controller's internal level 1 has 0 weight, load taken = 0
        assertEqual("Halt: lift at level 1", 1, lift.getCurrentLevel());

        pass("testControllerHandleHalt");
    }

    // ─────────── Integration Test ───────────

    static void testFullSimulation() {
        LiftController controller = LiftControllerFactory.createLiftController(
            3, 10, "CLOSEST", List.of(1000, 1000, 1000)
        );

        // Simulate 3 passengers
        Tower tower = new Tower(10);

        // Passenger 0: level 0 -> level 5 (UP)
        tower.getLevel(0).addWeight(70);
        controller.handleOutsideCall(new OutsideCall(0, Movement.UP));

        // Passenger 1: level 3 -> level 7 (UP)
        tower.getLevel(3).addWeight(85);
        controller.handleOutsideCall(new OutsideCall(3, Movement.UP));

        // Passenger 2: level 8 -> level 2 (DOWN)
        tower.getLevel(8).addWeight(60);
        controller.handleOutsideCall(new OutsideCall(8, Movement.UP));

        // Run simulation for 20 steps
        for (int i = 0; i < 20; i++) {
            controller.step();
        }

        // After 20 steps all lifts should have completed their requests
        boolean allIdle = true;
        for (Lift lift : controller.getLifts()) {
            if (!lift.isIdle()) {
                allIdle = false;
                break;
            }
        }
        assertTrue("Full sim: all lifts idle after 20 steps", allIdle);

        pass("testFullSimulation");
    }

    // ─────────── Helpers ───────────

    static void assertEqual(String label, Object expected, Object actual) {
        if (!Objects.equals(expected, actual)) {
            System.out.println("  FAIL: " + label + " — expected " + expected + " but got " + actual);
            throw new AssertionError(label);
        }
    }

    static void assertTrue(String label, boolean condition) {
        if (!condition) {
            System.out.println("  FAIL: " + label + " — expected true");
            throw new AssertionError(label);
        }
    }

    static void assertFalse(String label, boolean condition) {
        if (condition) {
            System.out.println("  FAIL: " + label + " — expected false");
            throw new AssertionError(label);
        }
    }

    static void assertNotNull(String label, Object obj) {
        if (obj == null) {
            System.out.println("  FAIL: " + label + " — expected non-null");
            throw new AssertionError(label);
        }
    }

    static void pass(String testName) {
        passed++;
        System.out.println("  PASS: " + testName);
    }

    static void fail(String testName, String reason) {
        failed++;
        System.out.println("  FAIL: " + testName + " — " + reason);
    }

    static class AssertionError extends RuntimeException {
        AssertionError(String msg) {
            super(msg);
            failed++;
        }
    }
}
