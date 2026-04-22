package com.erzbir.injector.halo.filter;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

final class HTMLResponseCache {
    private static final int RESPONSE_CACHE_MAX_SIZE = 1024;
    private static final int FINGERPRINT_CACHE_MAX_SIZE = 1;
    private static final Duration TTL = Duration.ofDays(1);

    private static final ConcurrentHashMap<String, Cache<Long, String>> RESPONSE_CACHE =
        new ConcurrentHashMap<>();

    public static String get(String path, long fingerprint) {
        Cache<Long, String> inner = RESPONSE_CACHE.get(path);
        if (inner == null) {
            return null;
        }
        return inner.getIfPresent(fingerprint);
    }

    public static void put(String path, long fingerprint, String processedHtml) {
        Cache<Long, String> inner = RESPONSE_CACHE.computeIfAbsent(path, k ->
            Caffeine.newBuilder()
                .maximumSize(FINGERPRINT_CACHE_MAX_SIZE)
                .expireAfterWrite(TTL)
                .build()
        );
        inner.put(fingerprint, processedHtml);
        if (RESPONSE_CACHE.size() > RESPONSE_CACHE_MAX_SIZE) {
            RESPONSE_CACHE.keys().asIterator().forEachRemaining(k -> {
                if (RESPONSE_CACHE.size() <= RESPONSE_CACHE_MAX_SIZE) {
                    return;
                }
                RESPONSE_CACHE.remove(k);
            });
        }
    }

    public static void invalidateCache(String path) {
        RESPONSE_CACHE.remove(path);
    }
}