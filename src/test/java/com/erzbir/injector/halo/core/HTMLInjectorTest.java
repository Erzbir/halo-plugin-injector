package com.erzbir.injector.halo.core;

import com.erzbir.injector.api.Code;
import com.erzbir.injector.api.IInjectionRule;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class HTMLInjectorTest {

    @Test
    void shouldWrapCodeWithPluginMarkers_whenUsingDefaultProcessCode() {
        HTMLInjector injector = (target, code, rule, context) -> target.html();

        String result = injector.processCode("<script>x</script>");

        assertEquals("<!-- PluginInjector start --><script>x</script><!-- PluginInjector end -->", result);
    }

    @Test
    void shouldReturnInjectedValue_whenImplementationIsIdentity() {
        HTMLInjector injector = (target, code, rule, context) -> target.html();
        Document document = new Document("");
        Element root = document.appendElement("div");
        root.text("ok");
        Code code = () -> "<script>x</script>";
        IInjectionRule rule = mock(IInjectionRule.class);

        String result = injector.inject(document, code, rule, null);

        assertEquals(document.html(), result);
    }
}
