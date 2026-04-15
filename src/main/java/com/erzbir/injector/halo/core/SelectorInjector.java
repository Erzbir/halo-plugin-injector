package com.erzbir.injector.halo.core;

import com.erzbir.injector.api.Code;
import com.erzbir.injector.api.IInjectionRule;
import com.erzbir.injector.halo.util.HTMLInjectUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

/**
 * @author Erzbir
 * @since 1.0.0
 */
@Component
public class SelectorInjector implements HTMLInjector {

    @Override
    public String inject(String html, Code code, IInjectionRule rule, Void context) {
        Document doc = Jsoup.parse(html);

        Elements elements = doc.select(rule.getMatch());
        if (elements.isEmpty()) {
            return html;
        }

        for (Element element : elements) {
            HTMLInjectUtil.inject(element, processCode(code.raw()), rule.getPosition());
        }

        doc.outputSettings(new Document.OutputSettings().prettyPrint(false));

        return doc.html();
    }
}
