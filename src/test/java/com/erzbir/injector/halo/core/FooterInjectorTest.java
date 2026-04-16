package com.erzbir.injector.halo.core;

import org.junit.jupiter.api.Test;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IModel;
import org.thymeleaf.model.IModelFactory;
import org.thymeleaf.model.IText;

import static org.mockito.Mockito.*;

class FooterInjectorTest {

    @Test
    void shouldAddProcessedCodeToModel() {
        FooterInjector injector = new FooterInjector();
        ITemplateContext context = mock(ITemplateContext.class);
        IModel model = mock(IModel.class);
        IModelFactory modelFactory = mock(IModelFactory.class);
        IText text = mock(IText.class);
        when(context.getModelFactory()).thenReturn(modelFactory);
        when(modelFactory.createText("<!-- PluginInjector start --><script>f</script><!-- PluginInjector end -->"))
                .thenReturn(text);

        injector.inject(model, new HTMLCode("<script>f</script>"), null, context);

        verify(model).add(text);
    }
}
