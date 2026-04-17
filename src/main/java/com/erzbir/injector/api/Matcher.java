package com.erzbir.injector.api;

public interface Matcher<TARGET, RULE> {
    boolean match(TARGET target, RULE rule);
}
