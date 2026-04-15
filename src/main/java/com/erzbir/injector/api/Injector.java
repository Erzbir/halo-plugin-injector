package com.erzbir.injector.api;

/**
 * @author Erzbir
 * @since 1.0.0
 */
public interface Injector<TARGET, CONTEXT> {

    default String processCode(String code) {
        String START_MARK = "<!-- PluginInjector start -->";
        String END_MAR = "<!-- PluginInjector end -->";
        return START_MARK + code + END_MAR;
    }

    TARGET inject(TARGET target, Code code, IInjectionRule rule, CONTEXT context);
}
