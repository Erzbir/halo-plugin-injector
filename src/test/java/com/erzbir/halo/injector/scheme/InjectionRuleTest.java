package com.erzbir.halo.injector.scheme;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InjectionRuleTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    // why: DOM 注入规则即使会退化成全站 HTML 处理，也应允许保存，由 UI 给出性能警告即可。
    @Test
    void shouldAllowUnsupportedDomRuleWhenConvertedToInjectionRule() {
        InjectionRule rule = assertDoesNotThrow(
                () -> objectMapper.convertValue(
                        Map.of(
                                "mode", "SELECTOR",
                                "match", "main",
                                "matchRule", Map.of(
                                        "type", "GROUP",
                                        "operator", "OR",
                                        "children", List.of(
                                                Map.of(
                                                        "type", "PATH",
                                                        "matcher", "ANT",
                                                        "value", "/posts/**"
                                                ),
                                                Map.of(
                                                        "type", "TEMPLATE_ID",
                                                        "matcher", "EXACT",
                                                        "value", "post"
                                                )
                                        )
                                )
                        ),
                        InjectionRule.class
                )
        );

        assertTrue(rule.isValid());
    }
}
