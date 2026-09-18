package com.smbc.driving.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * A car that can be placed on the field with a command sequence.
 */
public final class Car {

    private final String name;
    private Position position;
    private Direction direction;
    private final String commands;
    private boolean collided;
    private String collisionPartner;
    private Integer collisionStep;
    private Position collisionPosition;

    public Car(String name, Position position, Direction direction, String commands) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Car name must not be blank.");
        }
        Objects.requireNonNull(position, "position");
        Objects.requireNonNull(direction, "direction");
        this.name = name.trim();
        this.position = position;
        this.direction = direction;
        this.commands = normalizeCommands(commands);
        this.collided = false;
    }

    private static String normalizeCommands(String raw) {
        if (raw == null || raw.isBlank()) {
            return "";
        }
        String cleaned = raw.trim().toUpperCase(Locale.ROOT).replaceAll("\\s+", "");
        for (int i = 0; i < cleaned.length(); i++) {
            char c = cleaned.charAt(i);
            if (c != 'L' && c != 'R' && c != 'F') {
                throw new IllegalArgumentException(
                        "Invalid command '" + c + "'. Only L, R and F are allowed.");
            }
        }
        return cleaned;
    }

    public String name() {
        return name;
    }

    public Position position() {
        return position;
    }

    public Direction direction() {
        return direction;
    }

    public String commands() {
        return commands;
    }

    public boolean hasCollided() {
        return collided;
    }

    public String collisionPartner() {
        return collisionPartner;
    }

    public Integer collisionStep() {
        return collisionStep;
    }

    public Position collisionPosition() {
        return collisionPosition;
    }

    public void markCollision(String partner, int step, Position at) {
        this.collided = true;
        this.collisionPartner = partner;
        this.collisionStep = step;
        this.collisionPosition = at;
    }

    /**
     * Applies a single command. Forward moves that would leave the field are ignored.
     * Once a car has collided it no longer moves.
     */
    public void apply(char command, Field field) {
        if (collided) {
            return;
        }
        switch (command) {
            case 'L' -> direction = direction.turnLeft();
            case 'R' -> direction = direction.turnRight();
            case 'F' -> {
                Position next = new Position(
                        position.x() + direction.dx(),
                        position.y() + direction.dy());
                if (field.contains(next)) {
                    position = next;
                }
            }
            default -> throw new IllegalStateException("Unexpected command: " + command);
        }
    }

    public char commandAt(int stepIndex) {
        if (stepIndex < 0 || stepIndex >= commands.length()) {
            return 0;
        }
        return commands.charAt(stepIndex);
    }

    public int commandCount() {
        return commands.length();
    }

    public String describeInitial() {
        return "- " + name + ", " + position + " " + direction + ", " + commands;
    }

    public String describeResult() {
        if (collided) {
            return "- " + name + ", collides with " + collisionPartner
                    + " at " + collisionPosition + " at step " + collisionStep;
        }
        return "- " + name + ", " + position + " " + direction;
    }

    public Car snapshot() {
        Car copy = new Car(name, position, direction, commands);
        if (collided) {
            copy.markCollision(collisionPartner, collisionStep, collisionPosition);
        }
        return copy;
    }

    public static List<Car> snapshotsOf(List<Car> cars) {
        List<Car> copies = new ArrayList<>(cars.size());
        for (Car car : cars) {
            copies.add(car.snapshot());
        }
        return Collections.unmodifiableList(copies);
    }
}
