package com.erzbir.injector.halo.process;

import com.erzbir.injector.api.InjectMode;
import com.erzbir.injector.halo.core.InjectHelper;
import com.erzbir.injector.halo.scheme.InjectionRule;
import org.junit.jupiter.api.Test;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IModel;
import org.thymeleaf.model.IModelFactory;
import org.thymeleaf.model.IText;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InjectorFooterProcessorTest {

    @Test
    void shouldProcessMatchedRulesWithFooterMode() {
        InjectHelper injectHelper = mock(InjectHelper.class);
        InjectorFooterProcessor processor = new InjectorFooterProcessor(injectHelper);
        ITemplateContext context = mock(ITemplateContext.class);
        IModel model = mock(IModel.class);
        IModelFactory modelFactory = mock(IModelFactory.class);
        IText text = mock(IText.class);
        InjectionRule rule = mock(InjectionRule.class);
        when(rule.getId()).thenReturn("r1");
        when(injectHelper.getMatchedRules("", InjectMode.FOOTER)).thenReturn(Flux.just(rule));
        when(injectHelper.getConcatCode(rule)).thenReturn(Mono.just("<script>f</script>"));
        when(context.getModelFactory()).thenReturn(modelFactory);
        when(modelFactory.createText("<!-- PluginInjector start --><script>f</script><!-- PluginInjector end -->"))
                .thenReturn(text);

        processor.process(context, null, null, model).block();

        verify(model).add(text);
    }
}
