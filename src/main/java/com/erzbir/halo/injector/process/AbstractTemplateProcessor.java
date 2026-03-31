package com.erzbir.halo.injector.process;

import com.erzbir.halo.injector.util.InjectHelper;
import com.erzbir.halo.injector.manager.CodeSnippetManager;
import com.erzbir.halo.injector.scheme.InjectionRule;
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

    protected final InjectHelper injectHelper;
    protected final CodeSnippetManager codeSnippetManager;

    protected abstract InjectionRule.Mode mode();

    protected abstract void doProcess(ITemplateContext context, IModel model, String code);


    protected Mono<Void> processInternal(ITemplateContext context, IModel model) {
        String path = ContextUtil.getPath(context);

        return injectHelper.getMatchedRules(path, mode())
                .flatMap(rule ->
                        injectHelper.getConcatCode(rule)
                                .doOnNext(code -> {
                                    doProcess(context, model, code);
                                }).doOnSuccess(s -> log.debug("Injected rule: [{}] into [{}]",
                                        rule.getId(),
                                        path))
                )
                .then();
    }
}
