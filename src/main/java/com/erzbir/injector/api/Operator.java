package com.erzbir.injector.api;

import lombok.Getter;

@Getter
public enum Operator {
    AND(false, null),
    OR(false, null),
    NOT(true, AND),
    AND_NOT(true, AND),
    OR_NOT(true, OR);

    private final boolean negated;
    private final Operator connector;

    Operator(boolean negated, Operator connector) {
        this.negated = negated;
        this.connector = connector == null ? this : connector;
    }
}
