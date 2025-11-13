package com.erzbir.halo.injector.core;

import com.erzbir.halo.injector.util.InjectUtil;
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
    public String inject(String html, InjectionRule rule) {
        Document doc = Jsoup.parse(html);

        Elements elements = doc.select(rule.getSelector());
        if (elements.isEmpty()) {
            return html;
        }

        for (Element element : elements) {
            InjectUtil.inject(element, processCode(rule.getCode()), rule.getPosition());
        }

        doc.outputSettings(new Document.OutputSettings().prettyPrint(false));

        return doc.html();
    }
}
