package com.erzbir.injector.halo.scheme;

import com.erzbir.injector.api.InjectMode;
import com.erzbir.injector.api.MatchRule;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InjectionRuleTest {

    @Test
    void shouldRequireNonBlankMatchWhenModeIsId() {
        InjectionRule rule = new InjectionRule();
        rule.setMode(InjectMode.ID);
        rule.setMatch(" ");

        assertFalse(rule.isValid());

        rule.setMatch("content-root");

        assertTrue(rule.isValid());
    }

    @Test
    void shouldRequireNonBlankMatchWhenModeIsSelector() {
        InjectionRule rule = new InjectionRule();
        rule.setMode(InjectMode.SELECTOR);
        rule.setMatch("");

        assertFalse(rule.isValid());

        rule.setMatch(".article-body");

        assertTrue(rule.isValid());
    }

    @Test
    void shouldValidateByMatchRuleWhenModeIsHeadOrFooter() {
        InjectionRule rule = new InjectionRule();
        rule.setMode(InjectMode.HEAD);
        rule.setMatchRule(MatchRule.groupRule(MatchRule.Operator.AND));

        assertFalse(rule.isValid());

        rule.setMatchRule(MatchRule.defaultRule());

        assertTrue(rule.isValid());
    }

    @Test
    void shouldFallbackToDefaultMatchRuleWhenRuleMatchRuleIsNull() {
        InjectionRule rule = new InjectionRule();
        rule.setMatchRule(null);

        MatchRule matchRule = rule.getMatchRule();

        assertNotNull(matchRule);
        assertTrue(matchRule.isValid());
    }
}
