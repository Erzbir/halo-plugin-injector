package com.erzbir.halo.injector.process;

import com.erzbir.halo.injector.core.HeadInjector;
import com.erzbir.halo.injector.core.InjectService;
import com.erzbir.halo.injector.core.InjectionRule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IModel;
import org.thymeleaf.processor.element.IElementModelStructureHandler;
import reactor.core.publisher.Mono;
import run.halo.app.theme.dialect.TemplateHeadProcessor;

/**
 * @author Erzbir
 * @since 1.0.0
 */
@Slf4j
@Component
public class InjectorHeadProcessor extends AbstractTemplateProcessor
    implements TemplateHeadProcessor {
    private final HeadInjector headInjector;

    public InjectorHeadProcessor(InjectService injectService,
        HeadInjector headInjector) {
        super(injectService);
        this.headInjector = headInjector;
    }

    @Override
    public Mono<Void> process(ITemplateContext context, IModel model,
        IElementModelStructureHandler structureHandler) {
        return processInternal(context, model);
    }

    @Override
    protected InjectionRule.Mode mode() {
        return InjectionRule.Mode.HEAD;
    }

    @Override
    protected void doProcess(ITemplateContext context, IModel model, InjectionRule rule) {
        headInjector.inject(context, model, rule);
    }
}
