package com.erzbir.halo.injector.core;

/**
 * @author Erzbir
 * @since 1.0.0
 */
public interface Injector {
    String START_MARK = "<!-- PluginInjector start -->";
    String END_MARK = "<!-- PluginInjector end -->";

    default String processCode(String code) {
        return processCode(code, true);
    }

    default String processCode(String code, boolean wrapMarker) {
        if (!wrapMarker) {
            return code;
        }
        return START_MARK + code + END_MARK;
    }
}
