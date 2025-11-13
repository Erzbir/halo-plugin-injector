package com.erzbir.halo.injector.core;

/**
 * @author Erzbir
 * @since 1.0.0
 */
public interface Injector {

    default String processCode(String code) {
        String START_MARK = "<!-- PluginInjector start -->";
        String END_MAR = "<!-- PluginInjector end -->";
        return START_MARK + code + END_MAR;
    }
}
