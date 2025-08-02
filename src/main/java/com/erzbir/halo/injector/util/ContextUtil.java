package com.erzbir.halo.injector.util;

import org.thymeleaf.context.Contexts;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.web.IWebRequest;

/**
 * @author Erzbir
 * @since 1.0.0
 */
public class ContextUtil {
    public static String getPath(ITemplateContext context) {
        try {
            if (!Contexts.isWebContext(context)) {
                return "";
            }
            IWebRequest request = Contexts.asWebContext(context).getExchange().getRequest();
            return request.getRequestPath();
        } catch (Exception e) {
            return "";
        }
    }
}
