package com.erzbir.halo.injector.util;

import com.erzbir.halo.injector.scheme.InjectionRule;
import com.erzbir.halo.injector.scheme.MatchRule;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InjectionRuleValidatorTest {
    private final InjectionRuleValidator validator = new InjectionRuleValidator();

    @Test
    void shouldAllowValidRegexDuringWriteValidation() {
        InjectionRule rule = makeRule();
        rule.setMatchRule(makeGroup(MatchRule.pathRule(MatchRule.Matcher.REGEX, "^/posts/\\d+$")));

        assertDoesNotThrow(() -> validator.validateForWrite(rule).block());
    }

    @Test
    void shouldRejectInvalidRegexDuringWriteValidation() {
        InjectionRule rule = makeRule();
        rule.setMatchRule(makeGroup(MatchRule.pathRule(MatchRule.Matcher.REGEX, "[")));

        InjectionRuleValidationException error = assertThrows(
                InjectionRuleValidationException.class,
                () -> validator.validateForWrite(rule).block()
        );

        assertEquals("matchRule.children[0].value：正则表达式无效，Unclosed character class", error.getReason());
    }

    @Test
    void shouldRejectTemplateIdAntMatcherDuringWriteValidation() {
        InjectionRule rule = makeRule();
        MatchRule child = MatchRule.templateRule(MatchRule.Matcher.EXACT, "post");
        child.setMatcher(MatchRule.Matcher.ANT);
        rule.setMatchRule(makeGroup(child));

        InjectionRuleValidationException error = assertThrows(
                InjectionRuleValidationException.class,
                () -> validator.validateForWrite(rule).block()
        );

        assertEquals("matchRule.children[0].matcher：模板 ID 仅支持 REGEX 或 EXACT", error.getReason());
    }

    @Test
    void shouldRequireGroupAsRootNode() {
        InjectionRule rule = makeRule();
        rule.setMatchRule(MatchRule.pathRule(MatchRule.Matcher.ANT, "/**"));

        InjectionRuleValidationException error = assertThrows(
                InjectionRuleValidationException.class,
                () -> validator.validateForWrite(rule).block()
        );

        assertEquals("matchRule.type：根节点必须是 GROUP", error.getReason());
    }

    private InjectionRule makeRule() {
        InjectionRule rule = new InjectionRule();
        rule.setMatchRule(MatchRule.defaultRule());
        return rule;
    }

    private MatchRule makeGroup(MatchRule child) {
        MatchRule group = new MatchRule();
        group.setType(MatchRule.Type.GROUP);
        group.setChildren(java.util.List.of(child));
        return group;
    }
}
