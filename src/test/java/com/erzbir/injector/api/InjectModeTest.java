package com.erzbir.injector.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InjectModeTest {

    @ParameterizedTest
    @EnumSource(InjectMode.class)
    void shouldResolveEnumByName_whenNameIsValid(InjectMode mode) {
        InjectMode result = InjectMode.valueOf(mode.name());

        assertEquals(mode, result);
    }

    @ParameterizedTest
    @ValueSource(strings = {"head", "invalid", " HEAD ", "123", ""})
    void shouldThrowException_whenNameIsInvalid(String value) {
        assertThrows(IllegalArgumentException.class, () -> InjectMode.valueOf(value));
    }

    @Test
    void shouldThrowException_whenNameIsNull() {
        assertThrows(NullPointerException.class, () -> InjectMode.valueOf(null));
    }

    @Test
    void shouldContainExpectedEnumOrder() {
        assertEquals(InjectMode.HEAD, InjectMode.values()[0]);
        assertEquals(InjectMode.FOOTER, InjectMode.values()[1]);
        assertEquals(InjectMode.ID, InjectMode.values()[2]);
        assertEquals(InjectMode.SELECTOR, InjectMode.values()[3]);
    }
}
