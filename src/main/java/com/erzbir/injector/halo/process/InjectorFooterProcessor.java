package com.erzbir.injector.halo.process;

import com.erzbir.injector.api.InjectMode;
import com.erzbir.injector.halo.core.FooterInjector;
import com.erzbir.injector.halo.core.HTMLCode;
import com.erzbir.injector.halo.core.InjectHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IModel;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import reactor.core.publisher.Mono;
import run.halo.app.theme.dialect.TemplateFooterProcessor;

/**
 * @author Erzbir
 * @since 1.0.0
 */
@Slf4j
@Component
public class InjectorFooterProcessor extends AbstractTemplateProcessor
        implements TemplateFooterProcessor {
    private final FooterInjector footerInjector = new FooterInjector();

    public InjectorFooterProcessor(InjectHelper injectHelper) {
        super(injectHelper);
    }

    @Override
    public Mono<Void> process(ITemplateContext context, IProcessableElementTag tag,
                              IElementTagStructureHandler structureHandler, IModel model) {
        return processInternal(context, model);
    }

    @Override
    protected InjectMode mode() {
        return InjectMode.FOOTER;
    }

    @Override
    protected void doProcess(ITemplateContext context, IModel model, String code) {
        footerInjector.inject(model, new HTMLCode(code), null, context);
    }
}
