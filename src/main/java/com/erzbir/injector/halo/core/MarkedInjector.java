package com.erzbir.injector.halo.core;

import com.erzbir.injector.api.Injector;

public interface MarkedInjector<RETURN, TARGET, CONTEXT> extends Injector<RETURN, TARGET, CONTEXT> {
    String START_MARK = "<!-- PluginInjector start -->";
    String END_MARK = "<!-- PluginInjector end -->";

    @Override
    default String processCode(String code) {
        if (code.startsWith(START_MARK) && code.endsWith(END_MARK)) {
            return code;
        }
        return START_MARK + code + END_MARK;
    }
}
