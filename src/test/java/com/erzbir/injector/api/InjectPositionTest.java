package com.erzbir.injector.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InjectPositionTest {

    @ParameterizedTest
    @EnumSource(InjectPosition.class)
    void shouldResolveEnumByName_whenNameIsValid(InjectPosition position) {
        InjectPosition result = InjectPosition.valueOf(position.name());

        assertEquals(position, result);
    }

    @ParameterizedTest
    @ValueSource(strings = {"append", "invalid", " APPEND ", "123", ""})
    void shouldThrowException_whenNameIsInvalid(String value) {
        assertThrows(IllegalArgumentException.class, () -> InjectPosition.valueOf(value));
    }

    @Test
    void shouldThrowException_whenNameIsNull() {
        assertThrows(NullPointerException.class, () -> InjectPosition.valueOf(null));
    }

    @Test
    void shouldContainExpectedEnumOrder() {
        assertEquals(InjectPosition.APPEND, InjectPosition.values()[0]);
        assertEquals(InjectPosition.PREPEND, InjectPosition.values()[1]);
        assertEquals(InjectPosition.BEFORE, InjectPosition.values()[2]);
        assertEquals(InjectPosition.AFTER, InjectPosition.values()[3]);
        assertEquals(InjectPosition.REPLACE, InjectPosition.values()[4]);
    }
}
