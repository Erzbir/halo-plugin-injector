package com.erzbir.halo.injector.process;

import lombok.RequiredArgsConstructor;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IModel;
import org.thymeleaf.processor.element.IElementModelStructureHandler;

/**
 * @author Erzbir
 * @since 1.0.0
 */
@RequiredArgsConstructor
public class HeadInjectorProvider implements InjectProvider {
    private final ITemplateContext context;
    private final IModel model;
    private final IElementModelStructureHandler structureHandler;

    @Override
    public void inject(String code) {
        model.add(context.getModelFactory().createText(code));
    }
}
