package com.erzbir.halo.injector.process;

import lombok.RequiredArgsConstructor;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IModel;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.IElementTagStructureHandler;

/**
 * @author Erzbir
 * @since 1.0.0
 */
@RequiredArgsConstructor
public class FooterInjectProvider implements InjectProvider {
    private final ITemplateContext context;
    private final IModel model;
    private final IElementTagStructureHandler tagStructureHandler;
    private final IProcessableElementTag tag;

    @Override
    public void inject(String code) {
        model.add(context.getModelFactory().createText(code));
    }
}
