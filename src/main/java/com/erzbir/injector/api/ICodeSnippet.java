package com.erzbir.injector.api;

import java.util.Set;

/**
 * @author Erzbir
 * @since 1.0.0
 */
public interface ICodeSnippet {
    String getId();

    String getName();

    String getDescription();

    String getCode();

    boolean isEnabled();

    Set<String> getRuleIds();
}
