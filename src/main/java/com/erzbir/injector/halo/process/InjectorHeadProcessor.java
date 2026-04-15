package com.erzbir.injector.halo.process;

import com.erzbir.injector.api.InjectMode;
import com.erzbir.injector.halo.core.HTMLCode;
import com.erzbir.injector.halo.core.HeadInjector;
import com.erzbir.injector.halo.core.InjectHelper;
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

    public InjectorHeadProcessor(InjectHelper injectHelper,
                                 HeadInjector headInjector) {
        super(injectHelper);
        this.headInjector = headInjector;
    }

    @Override
    public Mono<Void> process(ITemplateContext context, IModel model,
                              IElementModelStructureHandler structureHandler) {
        return processInternal(context, model);
    }

    @Override
    protected InjectMode mode() {
        return InjectMode.HEAD;
    }

    @Override
    protected void doProcess(ITemplateContext context, IModel model, String code) {
        headInjector.inject(model, new HTMLCode(code), null, context);
    }
}
