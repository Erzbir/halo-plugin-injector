package com.erzbir.injector.halo.core;


import lombok.Getter;
import org.springframework.http.server.PathContainer;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.SimpleRouteMatcher;
import org.springframework.web.util.pattern.PathPatternParser;
import org.springframework.web.util.pattern.PathPatternRouteMatcher;
import org.springframework.web.util.pattern.PatternParseException;

import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * @author Erzbir
 * @since 1.0.0
 */
@Getter
enum StringMatchers {
    ANT(AntMatcher.INSTANCE),

    REGEX(RegexMatcher.INSTANCE),

    EXACT(String::equals),

    PATH_PATTERN(PathPatternMatcher.INSTANCE);

    private final StringMatcher matcher;

    StringMatchers(StringMatcher matcher) {
        this.matcher = matcher;
    }

}

final class PathPatternMatcher implements StringMatcher {
    public static PathPatternMatcher INSTANCE = new PathPatternMatcher();

    private final PathPatternRouteMatcher patternMatcher;

    private PathPatternMatcher() {
        var parser = new PathPatternParser();
        parser.setPathOptions(PathContainer.Options.HTTP_PATH);
        this.patternMatcher = new PathPatternRouteMatcher(parser);
    }

    @Override
    public boolean match(String target, String rule) {
        try {
            return patternMatcher.match(rule, patternMatcher.parseRoute(target));
        } catch (PatternParseException e) {
            return false;
        }
    }
}

final class AntMatcher implements StringMatcher {
    public static AntMatcher INSTANCE = new AntMatcher();

    private final SimpleRouteMatcher antMatcher = new SimpleRouteMatcher(new AntPathMatcher());

    private AntMatcher() {

    }

    @Override
    public boolean match(String target, String rule) {
        try {
            return antMatcher.match(rule, antMatcher.parseRoute(target));
        } catch (Exception e) {
            return false;
        }
    }
}

final class RegexMatcher implements StringMatcher {
    public static RegexMatcher INSTANCE = new RegexMatcher();

    private final ConcurrentHashMap<String, Pattern> regexCache = new ConcurrentHashMap<>();

    private RegexMatcher() {

    }

    @Override
    public boolean match(String target, String rule) {
        try {
            return regexCache
                    .computeIfAbsent(rule, Pattern::compile)
                    .matcher(target)
                    .matches();
        } catch (PatternSyntaxException e) {
            return false;
        }
    }
}