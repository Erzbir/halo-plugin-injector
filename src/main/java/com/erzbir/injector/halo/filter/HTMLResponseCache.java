package com.erzbir.injector.halo.filter;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;

final class HTMLResponseCache {
    private static final int RESPONSE_CACHE_MAX_SIZE = 1024;
    private static final Duration TTL = Duration.ofDays(1);

    private static final Cache<CacheKey, String> RESPONSE_CACHE = Caffeine.newBuilder()
        .maximumSize(RESPONSE_CACHE_MAX_SIZE)
        .expireAfterWrite(TTL)
        .build();

    public static String get(String path, long fingerprint) {
        return RESPONSE_CACHE.getIfPresent(new CacheKey(path, fingerprint));
    }

    public static void put(String path, long fingerprint, String processedHtml) {
        RESPONSE_CACHE.put(new CacheKey(path, fingerprint), processedHtml);
    }

    public static void invalidateCache(String path) {
        RESPONSE_CACHE.asMap().keySet().removeIf(key -> key.path().equals(path));
    }

    static void clear() {
        RESPONSE_CACHE.invalidateAll();
    }

    private record CacheKey(String path, long fingerprint) {
    }
}
