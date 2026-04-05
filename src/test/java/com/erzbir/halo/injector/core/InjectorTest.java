package com.erzbir.halo.injector.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InjectorTest {
    private final Injector injector = new Injector() {
    };

    // why: 默认应保留注释标记，避免升级后无配置的旧规则行为发生变化。
    @Test
    void shouldWrapCodeWithMarkersByDefault() {
        assertEquals(
                "<!-- PluginInjector start --><script></script><!-- PluginInjector end -->",
                injector.processCode("<script></script>")
        );
    }

    // why: 用户显式关闭注释标记时，输出应保持原样，避免额外注释污染最终 HTML。
    @Test
    void shouldReturnOriginalCodeWhenMarkerDisabled() {
        assertEquals("<script></script>", injector.processCode("<script></script>", false));
    }
}
