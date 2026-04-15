package com.erzbir.injector.halo.core;

import com.erzbir.injector.api.Code;
import com.erzbir.injector.api.IInjectionRule;
import org.junit.jupiter.api.Test;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IModel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

class TemplateInjectorTest {

    @Test
    void shouldInheritProcessCodeDefaultMethod_whenUsingTemplateInjectorImplementation() {
        TemplateInjector injector = (target, code, rule, context) -> target;

        String result = injector.processCode("<script>x</script>");

        assertEquals("<!-- PluginInjector start --><script>x</script><!-- PluginInjector end -->", result);
    }

    @Test
    void shouldReturnSameModel_whenImplementationIsIdentity() {
        TemplateInjector injector = (target, code, rule, context) -> target;
        IModel model = mock(IModel.class);
        Code code = () -> "<script>x</script>";
        IInjectionRule rule = null;
        ITemplateContext context = mock(ITemplateContext.class);

        IModel result = injector.inject(model, code, rule, context);

        assertSame(model, result);
    }
}
