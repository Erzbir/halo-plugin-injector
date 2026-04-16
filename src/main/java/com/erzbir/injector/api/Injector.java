package com.erzbir.injector.api;

/**
 * @author Erzbir
 * @since 1.0.0
 */
public interface Injector<RETURN, TARGET, CONTEXT> {

    default String processCode(String code) {
        return code;
    }

    RETURN inject(TARGET target, Code code, IInjectionRule rule, CONTEXT context);
}
