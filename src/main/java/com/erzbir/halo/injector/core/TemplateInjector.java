package com.erzbir.halo.injector.core;

import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IModel;

/**
 * @author Erzbir
 * @since 1.0.0
 */
public interface TemplateInjector extends Injector {
    void inject(ITemplateContext context, IModel model, InjectionRule rule);
}
