package com.erzbir.halo.injector.filter;

import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.web.server.util.matcher.AndServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.MediaTypeServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.NegatedServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.stereotype.Component;

import java.util.Set;

import static org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers.pathMatchers;

@Component
public class PathMatcherFactory {
    private static final String[] INCLUDE_PATHS = {
            "/**"
    };

    private static final String[] EXCLUDED_PATHS = {
            "/console/**", "/uc/**", "/login/**",
            "/signup/**", "/logout/**", "/themes/**",
            "/plugins/**", "/actuator/**", "/api/**",
            "/apis/**", "/system/**",
            "/upload/**", "/webjars/**"
    };

    public ServerWebExchangeMatcher create() {
        var pathMatcher = pathMatchers(HttpMethod.GET, INCLUDE_PATHS);

        var excludeMatcher =
                new NegatedServerWebExchangeMatcher(pathMatchers(EXCLUDED_PATHS));

        var mediaTypeMatcher = new MediaTypeServerWebExchangeMatcher(MediaType.TEXT_HTML);
        mediaTypeMatcher.setIgnoredMediaTypes(Set.of(MediaType.ALL));

        return new AndServerWebExchangeMatcher(mediaTypeMatcher, excludeMatcher, pathMatcher);
    }
}