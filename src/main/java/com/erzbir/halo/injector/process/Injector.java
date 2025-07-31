package com.erzbir.halo.injector.process;

import com.erzbir.halo.injector.setting.InjectionRule;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IModel;
import reactor.core.publisher.Mono;

/**
 * @author Erzbir
 * @since 1.0.0
 */
public interface Injector {
    Mono<Void> inject(ITemplateContext context, IModel model,
        InjectionRule.Location location);
}
