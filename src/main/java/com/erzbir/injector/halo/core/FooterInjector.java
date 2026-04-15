package com.erzbir.injector.halo.core;

import com.erzbir.injector.api.Code;
import com.erzbir.injector.api.IInjectionRule;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IModel;

/**
 * @author Erzbir
 * @since 1.0.0
 */
@Component
public class FooterInjector implements TemplateInjector {
    @Override
    public IModel inject(IModel model, Code code, @Nullable IInjectionRule rule, ITemplateContext context) {
        model.add(context.getModelFactory().createText(processCode(code.raw())));
        return model;
    }
}