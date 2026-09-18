# Driving Simulation — Car Crash Java

CLI driving simulation for the SMBC Java Full-Stack assessment (via EY).

## How to run

```bash
chmod +x start.sh
./start.sh
```

Requirements:

- Java 21+ (`java` on `PATH`, `JAVA_HOME` recommended)
- Internet on first run (Gradle downloads dependencies)

`start.sh` always starts a **new process**. No field or car state is kept between runs.

### Run tests

```bash
./gradlew test
```

### Manual Gradle run

```bash
./gradlew run --console=plain
```

## What it does

1. Ask for field width and height (`x y`).
2. Let you add one or more cars (name, `x y Direction`, command string).
3. Run the simulation.
4. Print final positions or collision details.
5. Start over or exit.

Commands:

| Command | Meaning |
|---------|---------|
| `L` | Turn 90° left |
| `R` | Turn 90° right |
| `F` | Move forward one cell (ignored if it would leave the field) |

Directions: `N`, `E`, `S`, `W`.

## Sample walkthrough (Scenario 1)

```
Welcome to Car Crash Java!

Please enter the width and height of the simulation field in x y format:
10 10

You have created a field of 10 x 10.

Please choose from the following options:
[1] Add a car to field
[2] Run simulation

1

Please enter the name of the car:
A

Please enter initial position of car A in x y Direction format:
1 2 N

Please enter the commands for car A:
FFRFFFFRRL

...
After simulation, the result is:
- A, (5,4) S
```

Scenario 2 (cars A and B) collides at `(5,4)` on **step 7**, matching the brief.

## Design decisions & assumptions

These are the places the brief is ambiguous; choices are intentional and covered by tests.

1. **Field bounds**  
   Width `W` and height `H` mean valid cells are `(0,0)` … `(W-1, H-1)`.  
   A `10 10` field has top-right `(9,9)`, as stated in the problem.

2. **Lock-step execution**  
   Step `N` applies the `N`th command of every car that still has commands and has not collided.  
   This is how A and B meet at `(5,4)` on step 7 in the sample.

3. **Collisions**  
   After each step, if two or more non-collided cars share a cell, they collide there, are reported, and stop moving.  
   For two cars, the message matches the sample:  
   `- A, collides with B at (5,4) at step 7`

4. **Boundary moves**  
   An `F` that would leave the field is ignored; the car keeps position and direction.

5. **Uneven command lengths**  
   A car that finishes its command list early stays put while others continue.

6. **Fresh process / no persistence**  
   Nothing is written to disk. Restarting `./start.sh` clears all cars.

7. **Validation**  
   - Duplicate car names rejected  
   - Start position must be inside the field and unoccupied  
   - Commands may only contain `L`, `R`, `F` (case-insensitive; whitespace stripped)

8. **Stack choice**  
   Plain Java 21 + Gradle + JUnit 5. No simulation libraries. Domain (`model` / `engine`) is separate from the CLI so it stays easy to review and test.

## Project layout

```
start.sh
build.gradle
README.md
src/main/java/com/smbc/driving/
  model/     Field, Car, Direction, Position
  engine/    SimulationEngine
  cli/       DrivingSimulationCli (entry point)
src/test/java/...
```

## Deviations from the sample text

- The sample sometimes says “height” as “heigh” and once prompts “initial position of car A” while adding car B. The app uses correct spelling and the **current** car name in prompts.
- Extra validation messages were added so bad input does not crash the session.

## Author

Veeraputhiran S  
Prepared for the SMBC Java Full-Stack developer assessment.
