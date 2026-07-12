package com.erzbir.injector.halo;

import com.erzbir.injector.halo.scheme.CodeSnippet;
import com.erzbir.injector.halo.scheme.InjectionRule;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import run.halo.app.extension.Scheme;
import run.halo.app.extension.SchemeManager;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HaloInjectorPluginTest {

    @Test
    void shouldDeclareLoadableExtensionImplementations() throws IOException {
        try (var definitions = getClass().getResourceAsStream(
            "/extensions/extension-definitions.yaml")) {
            assertNotNull(definitions);
            String yaml = new String(definitions.readAllBytes(), StandardCharsets.UTF_8);

            assertTrue(yaml.contains("extensionPointName: template-head-processor"));
            assertTrue(yaml.contains(
                "className: com.erzbir.injector.halo.process.InjectorHeadProcessor"));
            assertTrue(yaml.contains(
                "className: com.erzbir.injector.halo.process.InjectorFooterProcessor"));
            assertTrue(yaml.contains(
                "className: com.erzbir.injector.halo.filter.InjectorWebFilter"));
            assertFalse(yaml.contains("className: com.erzbir.halo.injector"));
        }
    }

    @Test
    void shouldRegisterSchemesOnStartAndUnregisterOnStop() {
        SchemeManager schemeManager = mock(SchemeManager.class);
        Scheme codeSnippetScheme = mock(Scheme.class);
        Scheme injectionRuleScheme = mock(Scheme.class);
        when(schemeManager.get(CodeSnippet.class)).thenReturn(codeSnippetScheme);
        when(schemeManager.get(InjectionRule.class)).thenReturn(injectionRuleScheme);
        HaloInjectorPlugin plugin = new HaloInjectorPlugin(schemeManager);

        plugin.start();
        plugin.stop();

        verify(schemeManager).register(CodeSnippet.class);
        verify(schemeManager).register(InjectionRule.class);
        verify(schemeManager).unregister(codeSnippetScheme);
        verify(schemeManager).unregister(injectionRuleScheme);
    }
}
