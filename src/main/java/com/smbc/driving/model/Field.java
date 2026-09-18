package com.smbc.driving.model;

/**
 * Rectangular driving field.
 * <p>
 * Spec note: a field of width W and height H has valid positions
 * {@code (0,0)} through {@code (W-1, H-1)}. A 10x10 field therefore
 * has its top-right corner at (9,9).
 */
public final class Field {

    private final int width;
    private final int height;

    public Field(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException(
                    "Field width and height must both be positive integers.");
        }
        this.width = width;
        this.height = height;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public boolean contains(Position position) {
        return position.x() >= 0
                && position.y() >= 0
                && position.x() < width
                && position.y() < height;
    }

    @Override
    public String toString() {
        return width + " x " + height;
    }
}
