package com.smbc.driving.engine;

import com.smbc.driving.model.Car;
import com.smbc.driving.model.Field;
import com.smbc.driving.model.Position;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Runs the driving simulation one discrete step at a time.
 *
 * <p>Design decisions (also documented in README):
 * <ul>
 *   <li>Cars execute commands in lock-step. Step N applies the Nth command of every car
 *       that still has commands remaining and has not already collided.</li>
 *   <li>After each step, any grid cell occupied by two or more cars is a collision.
 *       All cars in that cell are frozen at that position and stop accepting further commands.</li>
 *   <li>A car that would move outside the field ignores that F command and stays put.</li>
 *   <li>If a car finishes its command list early it simply stays where it is while others continue.</li>
 * </ul>
 */
public final class SimulationEngine {

    private final Field field;
    private final List<Car> cars;

    public SimulationEngine(Field field, List<Car> cars) {
        this.field = Objects.requireNonNull(field, "field");
        this.cars = new ArrayList<>(Objects.requireNonNull(cars, "cars"));
        validatePlacement();
    }

    private void validatePlacement() {
        Map<Position, String> occupied = new HashMap<>();
        for (Car car : cars) {
            if (!field.contains(car.position())) {
                throw new IllegalArgumentException(
                        "Car " + car.name() + " starts outside the field at " + car.position());
            }
            String existing = occupied.put(car.position(), car.name());
            if (existing != null) {
                throw new IllegalArgumentException(
                        "Cars " + existing + " and " + car.name()
                                + " cannot start on the same position " + car.position());
            }
        }
    }

    public List<Car> run() {
        int maxSteps = cars.stream().mapToInt(Car::commandCount).max().orElse(0);

        for (int step = 1; step <= maxSteps; step++) {
            int commandIndex = step - 1;
            for (Car car : cars) {
                if (car.hasCollided()) {
                    continue;
                }
                char command = car.commandAt(commandIndex);
                if (command == 0) {
                    continue;
                }
                car.apply(command, field);
            }
            resolveCollisions(step);
        }
        return List.copyOf(cars);
    }

    private void resolveCollisions(int step) {
        Map<Position, List<Car>> byCell = new LinkedHashMap<>();
        for (Car car : cars) {
            if (car.hasCollided()) {
                // Already stopped; still occupy their collision cell for reporting only.
                // They do not participate in new collisions.
                continue;
            }
            byCell.computeIfAbsent(car.position(), ignored -> new ArrayList<>()).add(car);
        }

        for (Map.Entry<Position, List<Car>> entry : byCell.entrySet()) {
            List<Car> occupants = entry.getValue();
            if (occupants.size() < 2) {
                continue;
            }
            Position at = entry.getKey();
            for (Car car : occupants) {
                String partners = occupants.stream()
                        .filter(other -> other != car)
                        .map(Car::name)
                        .reduce((a, b) -> a + ", " + b)
                        .orElse("?");
                // Spec sample uses a single partner name for two-car crashes.
                if (occupants.size() == 2) {
                    partners = occupants.stream()
                            .filter(other -> other != car)
                            .map(Car::name)
                            .findFirst()
                            .orElse("?");
                }
                car.markCollision(partners, step, at);
            }
        }
    }
}
