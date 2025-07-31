package com.erzbir.halo.injector.process;

import com.erzbir.halo.injector.setting.InjectionRule;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IModel;
import org.thymeleaf.processor.element.IElementModelStructureHandler;
import reactor.core.publisher.Mono;
import run.halo.app.plugin.ReactiveSettingFetcher;
import run.halo.app.theme.dialect.TemplateHeadProcessor;

/**
 * @author Erzbir
 * @since 1.0.0
 */
@Component
public class HeadInjector extends AbstrictInjector implements TemplateHeadProcessor {

    public HeadInjector(ReactiveSettingFetcher reactiveSettingFetcher) {
        super(reactiveSettingFetcher);
    }

    @Override
    public Mono<Void> process(ITemplateContext context, IModel model,
        IElementModelStructureHandler structureHandler) {
        return inject(context, model, InjectionRule.Location.HEAD);
    }
}