package com.erzbir.halo.injector.core;

import lombok.Data;

import java.util.Set;

public interface IInjectionRule {
    String getId();

    String getName();

    String getDescription();

    boolean isEnabled();

    Mode getMode();

    String getMatch();

    Position getPosition();

    Set<String> getSnippetIds();

    Set<PathMatchRule> getPathPatterns();

    enum Mode {
        HEAD, FOOTER, ID, SELECTOR

    }

    enum Position {
        APPEND, PREPEND, BEFORE, AFTER, REPLACE
    }


    @Data
    class PathMatchRule {
        private String pathPattern = "";
    }

}