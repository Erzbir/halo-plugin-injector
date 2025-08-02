package com.erzbir.halo.injector.process;

import com.erzbir.halo.injector.setting.InjectionRule;

/**
 * @author Erzbir
 * @since 1.0.0
 */
public interface Injector {

    default String processRuleCode(InjectionRule rule) {
        return rule.getCode();
    }
}
