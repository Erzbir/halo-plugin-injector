package com.erzbir.halo.injector.process;

import com.erzbir.halo.injector.setting.InjectionRule;
import com.erzbir.halo.injector.util.ContextUtil;
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
@Component
public class HeadInjector extends AbstractTemplateInjector implements TemplateHeadProcessor {


    public HeadInjector(InjectService injectService) {
        super(injectService);
    }

    @Override
    public Mono<Void> process(ITemplateContext context, IModel model,
        IElementModelStructureHandler structureHandler) {
        return inject(new HeadInjectorProvider(context, model, structureHandler),
            ContextUtil.getPath(context),
            InjectionRule.Location.HEAD);
    }
}