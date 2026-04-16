package com.erzbir.halo.injector.core;

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

    MatchRule getMatchRule();

    enum Mode {
        HEAD, FOOTER, ID, SELECTOR

    }

    enum Position {
        APPEND, PREPEND, BEFORE, AFTER, REPLACE
    }

}
