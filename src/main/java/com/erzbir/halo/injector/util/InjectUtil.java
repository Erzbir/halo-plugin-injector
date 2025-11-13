package com.erzbir.halo.injector.util;

import com.erzbir.halo.injector.core.InjectionRule;
import org.jsoup.nodes.Element;

/**
 * @author Erzbir
 * @since 1.0.0
 */
public class InjectUtil {
    public static void inject(Element element, String code, InjectionRule.Position position) {
        switch (position) {
            case APPEND -> element.append(code);
            case PREPEND -> element.prepend(code);
            case BEFORE -> element.before(code);
            case AFTER -> element.after(code);
        }
    }
}
