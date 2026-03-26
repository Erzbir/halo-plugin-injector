package com.erzbir.halo.injector.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import java.util.List;

public class HTMLUtil {

    public static Document parse(String html) {
        return html == null ? new Document("") : Jsoup.parse(html);
    }

    public static Document parseBodyFragment(String html) {
        return html == null ? new Document("") : Jsoup.parseBodyFragment(html);
    }

    public static String toHtml(Document doc) {
        return doc == null ? "" : doc.outerHtml();
    }

    public static String toHtml(Node node) {
        return node == null ? "" : node.outerHtml();
    }

    public static String toPrettyHtml(Document doc) {
        if (doc == null) return "";
        doc.outputSettings().prettyPrint(true);
        return doc.outerHtml();
    }

    public static Element getById(Document doc, String id) {
        return (doc == null || id == null) ? null : doc.getElementById(id);
    }

    public static Elements select(Document doc, String cssQuery) {
        return (doc == null || cssQuery == null) ? new Elements() : doc.select(cssQuery);
    }

    public static Element selectFirst(Document doc, String cssQuery) {
        return (doc == null || cssQuery == null) ? null : doc.selectFirst(cssQuery);
    }

    public static Element getById(String html, String id) {
        return getById(parse(html), id);
    }

    public static Elements select(String html, String cssQuery) {
        return select(parse(html), cssQuery);
    }

    public static Element selectFirst(String html, String cssQuery) {
        return selectFirst(parse(html), cssQuery);
    }

    public static void replace(Element target, String html) {
        if (target == null || html == null) return;
        List<Node> nodes = parseBodyFragment(html).body().childNodes();
        if (nodes.isEmpty()) {
            target.remove();
        } else if (nodes.size() == 1) {
            target.replaceWith(nodes.get(0));
        } else {
            Element wrapper = new Element("span");
            wrapper.appendChildren(nodes);
            target.replaceWith(wrapper);
        }
    }

    public static void remove(Element el) {
        if (el != null) el.remove();
    }

    public static void setText(Element el, String text) {
        if (el != null) el.text(text == null ? "" : text);
    }

    public static void setHtml(Element el, String html) {
        if (el != null) el.html(html == null ? "" : html);
    }

    public static String attr(Element el, String key) {
        return (el == null || key == null) ? "" : el.attr(key);
    }

    public static void attr(Element el, String key, String value) {
        if (el != null && key != null) el.attr(key, value == null ? "" : value);
    }

    public static void removeAttr(Element el, String key) {
        if (el != null && key != null) el.removeAttr(key);
    }

    public static boolean exists(Element el) {
        return el != null;
    }

    public static boolean isEmpty(Elements els) {
        return els == null || els.isEmpty();
    }
}