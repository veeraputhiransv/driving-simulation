package com.smbc.driving.model;

/**
 * Cardinal directions a car can face on the field.
 * Ordered clockwise so L/R rotations stay simple.
 */
public enum Direction {
    N(0, 1),
    E(1, 0),
    S(0, -1),
    W(-1, 0);

    private final int dx;
    private final int dy;

    Direction(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }

    public int dx() {
        return dx;
    }

    public int dy() {
        return dy;
    }

    public Direction turnLeft() {
        Direction[] values = values();
        return values[(ordinal() + values.length - 1) % values.length];
    }

    public Direction turnRight() {
        Direction[] values = values();
        return values[(ordinal() + 1) % values.length];
    }

    public static Direction parse(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Direction is required (N, E, S or W).");
        }
        try {
            return Direction.valueOf(token.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    "Unknown direction '" + token + "'. Expected one of N, E, S, W.");
        }
    }
}
