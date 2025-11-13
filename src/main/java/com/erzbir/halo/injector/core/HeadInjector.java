package com.erzbir.halo.injector.core;

import org.springframework.stereotype.Component;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IModel;

/**
 * @author Erzbir
 * @since 1.0.0
 */
@Component
public class HeadInjector implements TemplateInjector {

    @Override
    public void inject(ITemplateContext context, IModel model, InjectionRule rule) {
        model.add(context.getModelFactory().createText(processCode(rule.getCode())));
    }
}