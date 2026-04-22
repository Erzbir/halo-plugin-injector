package com.erzbir.injector.halo.process;

import com.erzbir.injector.api.InjectMode;
import com.erzbir.injector.halo.core.InjectHelper;
import com.erzbir.injector.halo.util.ContextUtil;
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

    protected abstract InjectMode mode();

    protected abstract void doProcess(ITemplateContext context, IModel model, String code);


    protected Mono<Void> processInternal(ITemplateContext context, IModel model) {
        String path = ContextUtil.getPath(context);
        return injectHelper.getMatchedRules(path, mode())
            .concatMap(rule ->
                injectHelper.getConcatCode(rule)
                    .flatMap(code -> Mono.fromCallable(() -> {
                        doProcess(context, model, code);
                        return code;
                    }))
                    .doOnSuccess(
                        code -> log.debug("Injected rule [{}] into [{}]", rule.getId(), path))
                    .onErrorResume(e -> {
                        log.warn("Injection failed for path [{}] with rule [{}]", path,
                            rule.getId(), e);
                        return Mono.empty();
                    })
            )
            .then();
    }
}
