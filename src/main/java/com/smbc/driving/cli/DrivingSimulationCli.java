package com.smbc.driving.cli;

import com.smbc.driving.engine.SimulationEngine;
import com.smbc.driving.model.Car;
import com.smbc.driving.model.Direction;
import com.smbc.driving.model.Field;
import com.smbc.driving.model.Position;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Interactive CLI matching the sample sessions in the brief.
 */
public final class DrivingSimulationCli {

    private final BufferedReader in;
    private final PrintStream out;

    private Field field;
    private final List<Car> cars = new ArrayList<>();
    private final Set<String> usedNames = new HashSet<>();

    public DrivingSimulationCli(BufferedReader in, PrintStream out) {
        this.in = in;
        this.out = out;
    }

    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        new DrivingSimulationCli(reader, System.out).run();
    }

    public void run() throws IOException {
        out.println("Welcome to Car Crash Java!");
        out.println();

        boolean keepGoing = true;
        while (keepGoing) {
            resetSession();
            createField();
            keepGoing = runFieldSession();
        }

        out.println();
        out.println("Thank you for running the simulation. Goodbye!");
    }

    private void resetSession() {
        field = null;
        cars.clear();
        usedNames.clear();
    }

    private void createField() throws IOException {
        while (true) {
            out.println("Please enter the width and height of the simulation field in x y format:");
            String line = readLine();
            try {
                int[] wh = parseTwoInts(line);
                field = new Field(wh[0], wh[1]);
                out.println();
                out.println("You have created a field of " + field + ".");
                out.println();
                return;
            } catch (IllegalArgumentException ex) {
                out.println(ex.getMessage());
                out.println();
            }
        }
    }

    private boolean runFieldSession() throws IOException {
        while (true) {
            out.println("Please choose from the following options:");
            out.println("[1] Add a car to field");
            out.println("[2] Run simulation");
            out.println();

            String choice = readLine().trim();
            out.println();

            if ("1".equals(choice)) {
                addCar();
            } else if ("2".equals(choice)) {
                runSimulation();
                return askStartOverOrExit();
            } else {
                out.println("Please enter 1 or 2.");
                out.println();
            }
        }
    }

    private void addCar() throws IOException {
        String name;
        while (true) {
            out.println("Please enter the name of the car:");
            name = readLine().trim();
            out.println();
            if (name.isEmpty()) {
                out.println("Car name cannot be empty.");
                out.println();
                continue;
            }
            if (usedNames.contains(name.toLowerCase(Locale.ROOT))) {
                out.println("A car named '" + name + "' already exists. Please choose another name.");
                out.println();
                continue;
            }
            break;
        }

        Position position;
        Direction direction;
        while (true) {
            out.println("Please enter initial position of car " + name + " in x y Direction format:");
            String line = readLine();
            out.println();
            try {
                ParsedPlacement placement = parsePlacement(line);
                position = placement.position();
                direction = placement.direction();
                if (!field.contains(position)) {
                    throw new IllegalArgumentException(
                            "Position " + position + " is outside the field (" + field + ").");
                }
                for (Car existing : cars) {
                    if (existing.position().equals(position)) {
                        throw new IllegalArgumentException(
                                "Position " + position + " is already occupied by car "
                                        + existing.name() + ".");
                    }
                }
                break;
            } catch (IllegalArgumentException ex) {
                out.println(ex.getMessage());
                out.println();
            }
        }

        String commands;
        while (true) {
            out.println("Please enter the commands for car " + name + ":");
            String line = readLine();
            out.println();
            try {
                // Validate via Car constructor.
                Car probe = new Car(name, position, direction, line);
                commands = probe.commands();
                break;
            } catch (IllegalArgumentException ex) {
                out.println(ex.getMessage());
                out.println();
            }
        }

        Car car = new Car(name, position, direction, commands);
        cars.add(car);
        usedNames.add(name.toLowerCase(Locale.ROOT));

        printCarList("Your current list of cars are:");
        out.println();
    }

    private void runSimulation() {
        printCarList("Your current list of cars are:");
        out.println();

        if (cars.isEmpty()) {
            out.println("No cars on the field. Nothing to simulate.");
            out.println();
            return;
        }

        // Work on copies so "Start over" gets a clean registry via resetSession,
        // and a re-run within the same session doesn't mutate originals oddly.
        List<Car> runners = new ArrayList<>();
        for (Car car : cars) {
            runners.add(new Car(car.name(), car.position(), car.direction(), car.commands()));
        }

        List<Car> results = new SimulationEngine(field, runners).run();

        out.println("After simulation, the result is:");
        for (Car car : results) {
            out.println(car.describeResult());
        }
        out.println();
    }

    private boolean askStartOverOrExit() throws IOException {
        while (true) {
            out.println("Please choose from the following options:");
            out.println("[1] Start over");
            out.println("[2] Exit");
            out.println();
            String choice = readLine().trim();
            out.println();
            if ("1".equals(choice)) {
                return true;
            }
            if ("2".equals(choice)) {
                return false;
            }
            out.println("Please enter 1 or 2.");
            out.println();
        }
    }

    private void printCarList(String heading) {
        out.println(heading);
        if (cars.isEmpty()) {
            out.println("- (none)");
            return;
        }
        for (Car car : cars) {
            out.println(car.describeInitial());
        }
    }

    private String readLine() throws IOException {
        String line = in.readLine();
        if (line == null) {
            throw new IOException("Input closed unexpectedly.");
        }
        return line;
    }

    private static int[] parseTwoInts(String line) {
        String[] parts = split(line);
        if (parts.length != 2) {
            throw new IllegalArgumentException(
                    "Please enter exactly two integers: width and height (e.g. 10 10).");
        }
        try {
            return new int[]{Integer.parseInt(parts[0]), Integer.parseInt(parts[1])};
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(
                    "Width and height must be integers (e.g. 10 10).");
        }
    }

    private static ParsedPlacement parsePlacement(String line) {
        String[] parts = split(line);
        if (parts.length != 3) {
            throw new IllegalArgumentException(
                    "Please enter position as: x y Direction  (e.g. 1 2 N).");
        }
        try {
            int x = Integer.parseInt(parts[0]);
            int y = Integer.parseInt(parts[1]);
            Direction direction = Direction.parse(parts[2]);
            return new ParsedPlacement(new Position(x, y), direction);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(
                    "x and y must be integers (e.g. 1 2 N).");
        }
    }

    private static String[] split(String line) {
        if (line == null || line.isBlank()) {
            return new String[0];
        }
        return line.trim().split("\\s+");
    }

    private record ParsedPlacement(Position position, Direction direction) {
    }
}
