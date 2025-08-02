package com.erzbir.halo.injector.process;

import com.erzbir.halo.injector.setting.InjectionRule;
import com.erzbir.halo.injector.util.ContextUtil;
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
@Component
public class FooterInjector extends AbstractTemplateInjector implements TemplateFooterProcessor {

    public FooterInjector(InjectService injectService) {
        super(injectService);
    }

    @Override
    public Mono<Void> process(ITemplateContext context, IProcessableElementTag tag,
        IElementTagStructureHandler structureHandler, IModel model) {
        return inject(new FooterInjectProvider(context, model, structureHandler, tag),
            ContextUtil.getPath(context), InjectionRule.Location.FOOTER);
    }
}