package com.erzbir.injector.api;

import java.util.Set;

/**
 * @author Erzbir
 * @since 1.0.0
 */
public interface IInjectionRule {
    String getId();

    String getName();

    String getDescription();

    boolean isEnabled();

    InjectMode getMode();

    String getMatch();

    InjectPosition getPosition();

    Set<String> getSnippetIds();

    MatchRule getMatchRule();

}
