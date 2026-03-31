package com.erzbir.halo.injector.core;

import java.util.Set;

public interface ICodeSnippet {
    String getId();

    String getName();

    String getDescription();

    String getCode();

    boolean isEnabled();

    Set<String> getRuleIds();
}
