package com.erzbir.injector.halo;

import com.erzbir.injector.halo.scheme.CodeSnippet;
import com.erzbir.injector.halo.scheme.InjectionRule;
import org.junit.jupiter.api.Test;
import run.halo.app.extension.Scheme;
import run.halo.app.extension.SchemeManager;

import static org.mockito.Mockito.*;

class HaloInjectorPluginTest {

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
