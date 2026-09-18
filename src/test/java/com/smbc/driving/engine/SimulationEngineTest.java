package com.smbc.driving.engine;

import com.smbc.driving.model.Car;
import com.smbc.driving.model.Direction;
import com.smbc.driving.model.Field;
import com.smbc.driving.model.Position;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimulationEngineTest {

    @Test
    void scenarioOneSingleCarEndsAtFiveFourFacingSouth() {
        Field field = new Field(10, 10);
        Car a = new Car("A", new Position(1, 2), Direction.N, "FFRFFFFRRL");

        List<Car> result = new SimulationEngine(field, List.of(a)).run();

        assertEquals(1, result.size());
        Car finished = result.get(0);
        assertFalse(finished.hasCollided());
        assertEquals(new Position(5, 4), finished.position());
        assertEquals(Direction.S, finished.direction());
        assertEquals("- A, (5,4) S", finished.describeResult());
    }

    @Test
    void scenarioTwoCarsCollideAtFiveFourOnStepSeven() {
        Field field = new Field(10, 10);
        Car a = new Car("A", new Position(1, 2), Direction.N, "FFRFFFFRRL");
        Car b = new Car("B", new Position(7, 8), Direction.W, "FFLFFFFFFF");

        List<Car> result = new SimulationEngine(field, List.of(a, b)).run();

        Car ra = result.stream().filter(c -> c.name().equals("A")).findFirst().orElseThrow();
        Car rb = result.stream().filter(c -> c.name().equals("B")).findFirst().orElseThrow();

        assertTrue(ra.hasCollided());
        assertTrue(rb.hasCollided());
        assertEquals(new Position(5, 4), ra.collisionPosition());
        assertEquals(new Position(5, 4), rb.collisionPosition());
        assertEquals(7, ra.collisionStep());
        assertEquals(7, rb.collisionStep());
        assertEquals("B", ra.collisionPartner());
        assertEquals("A", rb.collisionPartner());
        assertEquals("- A, collides with B at (5,4) at step 7", ra.describeResult());
        assertEquals("- B, collides with A at (5,4) at step 7", rb.describeResult());
    }

    @Test
    void carThatFinishesCommandsEarlyStaysPutWhileOthersContinue() {
        Field field = new Field(5, 5);
        Car shortCar = new Car("S", new Position(0, 0), Direction.N, "F");
        Car longCar = new Car("L", new Position(2, 0), Direction.N, "FFF");

        List<Car> result = new SimulationEngine(field, List.of(shortCar, longCar)).run();

        Car s = result.stream().filter(c -> c.name().equals("S")).findFirst().orElseThrow();
        Car l = result.stream().filter(c -> c.name().equals("L")).findFirst().orElseThrow();
        assertEquals(new Position(0, 1), s.position());
        assertEquals(new Position(2, 3), l.position());
        assertFalse(s.hasCollided());
        assertFalse(l.hasCollided());
    }
}
