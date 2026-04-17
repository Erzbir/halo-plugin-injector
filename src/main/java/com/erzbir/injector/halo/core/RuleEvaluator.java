package com.erzbir.injector.halo.core;

import com.erzbir.injector.api.IMatchRule;

/**
 * @author Erzbir
 * @since 1.0.0
 */
interface RuleEvaluator {
    boolean evaluate(IMatchRule rule, String path);
}
