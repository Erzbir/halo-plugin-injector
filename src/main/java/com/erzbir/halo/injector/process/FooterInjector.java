package com.erzbir.halo.injector.process;

import com.erzbir.halo.injector.setting.InjectionRule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IModel;
import org.thymeleaf.model.IModelFactory;
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
@RequiredArgsConstructor
public class FooterInjector implements TemplateFooterProcessor {
    private final InjectionService injectionService;

    @Override
    public Mono<Void> process(ITemplateContext context, IProcessableElementTag tag,
        IElementTagStructureHandler structureHandler, IModel model) {
        return injectionService.getMatchedCodeForLocation(context, InjectionRule.Location.FOOTER)
            .doOnNext(code -> {
                if (!code.trim().isEmpty()) {
                    final IModelFactory modelFactory = context.getModelFactory();
                    model.add(modelFactory.createText(code));
                    log.debug("Injected FOOTER code: {}", code.length() > 100 ?
                        code.substring(0, 100) + "..." : code);
                }
            })
            .then();
    }
}