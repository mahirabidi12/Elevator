package com.elevator.model;

import java.util.*;

public class Tower {

    private final List<Level> levels;

    public Tower(int totalLevels) {
        levels = new ArrayList<>();

        for (int i = 0; i < totalLevels; i++) {
            levels.add(new Level(i));
        }
    }

    public Level getLevel(int levelNumber) {
        return levels.get(levelNumber);
    }

    public int getTotalLevels() {
        return levels.size();
    }
}
