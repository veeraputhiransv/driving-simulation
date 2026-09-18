package com.smbc.driving.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FieldTest {

    @Test
    void tenByTenFieldHasTopRightAtNineNine() {
        Field field = new Field(10, 10);
        assertTrue(field.contains(new Position(0, 0)));
        assertTrue(field.contains(new Position(9, 9)));
        assertFalse(field.contains(new Position(10, 9)));
        assertFalse(field.contains(new Position(9, 10)));
        assertFalse(field.contains(new Position(-1, 0)));
    }

    @Test
    void rejectsNonPositiveDimensions() {
        assertThrows(IllegalArgumentException.class, () -> new Field(0, 10));
        assertThrows(IllegalArgumentException.class, () -> new Field(10, -1));
    }
}

class DirectionTest {

    @Test
    void turnsClockwiseAndCounterClockwise() {
        assertEquals(Direction.W, Direction.N.turnLeft());
        assertEquals(Direction.E, Direction.N.turnRight());
        assertEquals(Direction.N, Direction.E.turnLeft());
        assertEquals(Direction.S, Direction.E.turnRight());
    }
}

class CarBoundaryTest {

    @Test
    void forwardBeyondBoundaryIsIgnored() {
        Field field = new Field(10, 10);
        Car car = new Car("A", new Position(0, 0), Direction.S, "F");
        car.apply('F', field);
        assertEquals(new Position(0, 0), car.position());
        assertEquals(Direction.S, car.direction());
    }
}
