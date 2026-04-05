package com.erzbir.halo.injector.scheme;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

class InjectionRuleTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    // why: 标准扩展资源写接口会经过 ObjectMapper.convertValue，模型层必须能兜底拦住非法 regex。
    @Test
    void shouldRejectInvalidRegexWhenConvertedToInjectionRule() {
        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> objectMapper.convertValue(
                        Map.of(
                                "mode", "FOOTER",
                                "matchRule", Map.of(
                                        "type", "GROUP",
                                        "operator", "AND",
                                        "children", List.of(
                                                Map.of(
                                                        "type", "PATH",
                                                        "matcher", "REGEX",
                                                        "value", "["
                                                )
                                        )
                                )
                        ),
                        InjectionRule.class
                )
        );

        assertTrue(error.getMessage().contains("Unclosed character class"));
    }

    // why: DOM 注入规则若不能按路径预筛，就会迫使所有 HTML 响应进入整页缓冲，模型层也必须拒绝。
    @Test
    void shouldRejectUnsupportedDomRuleWhenConvertedToInjectionRule() {
        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
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

        assertTrue(error.getMessage().contains("ID/SELECTOR 模式下必须可按路径预筛"));
    }

    // why: 新增字段对旧数据应保持兼容；未显式配置时仍默认输出注释标记。
    @Test
    void shouldDefaultWrapMarkerToTrueWhenFieldIsMissing() {
        InjectionRule rule = objectMapper.convertValue(
                Map.of(
                        "mode", "FOOTER",
                        "matchRule", Map.of(
                                "type", "GROUP",
                                "operator", "AND",
                                "children", List.of(
                                        Map.of(
                                                "type", "PATH",
                                                "matcher", "ANT",
                                                "value", "/**"
                                        )
                                )
                        )
                ),
                InjectionRule.class
        );

        assertEquals(true, rule.getWrapMarker());
    }
}
