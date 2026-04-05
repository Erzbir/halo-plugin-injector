package com.erzbir.halo.injector.core;

import com.erzbir.halo.injector.scheme.MatchRule;

import java.util.Set;

public interface IInjectionRule {
    String getId();

    String getName();

    String getDescription();

    boolean isEnabled();

    Mode getMode();

    String getMatch();

    MatchRule getMatchRule();

    Position getPosition();

    Set<String> getSnippetIds();

    boolean getWrapMarker();

    enum Mode {
        HEAD, FOOTER, ID, SELECTOR

    }

    enum Position {
        APPEND, PREPEND, BEFORE, AFTER, REPLACE, REMOVE
    }
}
