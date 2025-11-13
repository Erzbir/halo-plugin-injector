package com.erzbir.halo.injector.process;

import com.erzbir.halo.injector.core.InjectService;
import com.erzbir.halo.injector.core.InjectionRule;
import com.erzbir.halo.injector.util.ContextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IModel;
import reactor.core.publisher.Mono;

/**
 * @author Erzbir
 * @since 1.0.0
 */
@RequiredArgsConstructor
@Slf4j
public abstract class AbstractTemplateProcessor {

    private final InjectService injectService;

    protected abstract InjectionRule.Mode mode();

    protected abstract void doProcess(ITemplateContext context, IModel model, InjectionRule rule);


    protected Mono<Void> processInternal(ITemplateContext context, IModel model) {
        String path = ContextUtil.getPath(context);
        return injectService.getMatchedRules(path, mode())
            .doOnNext(rule -> {
                doProcess(context, model, rule);
                String code = rule.getCode();
                log.info("Injected code: [{}] into [{}]",
                    code.length() > 50 ? code.substring(0, 50) + "..." : code, path);
            })
            .then();
    }
}
