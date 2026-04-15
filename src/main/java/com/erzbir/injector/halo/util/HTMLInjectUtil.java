package com.erzbir.injector.halo.util;

import com.erzbir.injector.api.InjectPosition;
import org.jsoup.nodes.Element;

/**
 * @author Erzbir
 * @since 1.0.0
 */
public class HTMLInjectUtil {
    public static void inject(Element element, String code, InjectPosition position) {
        switch (position) {
            case APPEND -> element.append(code);
            case PREPEND -> element.prepend(code);
            case BEFORE -> element.before(code);
            case AFTER -> element.after(code);
            case REPLACE -> {
                element.after(code);
                element.remove();
            }
        }
    }
}
