package com.erzbir.injector.halo.util;

import lombok.extern.slf4j.Slf4j;
import org.thymeleaf.context.Contexts;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.web.IWebRequest;

/**
 * @author Erzbir
 * @since 1.0.0
 */
@Slf4j
public class ContextUtil {
    public static String getPath(ITemplateContext context) {
        try {
            if (!Contexts.isWebContext(context)) {
                return "";
            }
            IWebRequest request = Contexts.asWebContext(context).getExchange().getRequest();
            return request.getRequestPath();
        } catch (Exception e) {
            log.warn("Failed to resolve request path from template context, context: {}",
                context == null ? "null" : context.getClass().getName(), e);
            return "";
        }
    }
}
