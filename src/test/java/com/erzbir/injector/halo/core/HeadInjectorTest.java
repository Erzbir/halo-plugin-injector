package com.erzbir.injector.halo.core;

import org.junit.jupiter.api.Test;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IModel;
import org.thymeleaf.model.IModelFactory;
import org.thymeleaf.model.IText;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HeadInjectorTest {

    @Test
    void shouldAddProcessedCodeToModel() {
        HeadInjector injector = new HeadInjector();
        ITemplateContext context = mock(ITemplateContext.class);
        IModel model = mock(IModel.class);
        IModelFactory modelFactory = mock(IModelFactory.class);
        IText text = mock(IText.class);
        when(context.getModelFactory()).thenReturn(modelFactory);
        when(modelFactory.createText("<!-- PluginInjector start --><script>h</script><!-- PluginInjector end -->"))
                .thenReturn(text);

        injector.inject(model, new HTMLCode("<script>h</script>"), null, context);

        verify(model).add(text);
    }
}
