package com.erzbir.injector.halo.filter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

final class HTMLResponseCache {

    private static final long RESPONSE_CACHE_TTL_MILLIS = TimeUnit.DAYS.toMillis(1);
    private static final int RESPONSE_CACHE_MAX_SIZE = 1024;

    private static final ConcurrentHashMap<ResponseCacheKey, CachedResponse> responseCache = new ConcurrentHashMap<>();

    public static String get(String path, long fingerprint) {
        ResponseCacheKey cacheKey = new ResponseCacheKey(path, fingerprint);
        long now = System.currentTimeMillis();
        CachedResponse cached = responseCache.get(cacheKey);
        if (cached == null || cached.state() == CacheState.EXPIRE || cached.expiresAtMillis() <= now) {
            responseCache.remove(cacheKey);
            return null;
        }
        return cached.html();
    }

    public static void put(String path, long fingerprint, String processedHtml) {
        ResponseCacheKey cacheKey = new ResponseCacheKey(path, fingerprint);
        if (responseCache.size() >= RESPONSE_CACHE_MAX_SIZE) {
            responseCache.clear();
        }
        long expiresAtMillis = System.currentTimeMillis() + RESPONSE_CACHE_TTL_MILLIS;
        responseCache.put(cacheKey, new CachedResponse(expiresAtMillis, processedHtml, CacheState.VALID));
    }

    public static void invalidateCache(String path) {
        responseCache.replaceAll((k, v) -> {
            if (k.path().equals(path)) {
                return new CachedResponse(v.expiresAtMillis(), v.html(), CacheState.EXPIRE);
            }
            return v;
        });
    }

    private enum CacheState {
        VALID,
        EXPIRE
    }

    private record ResponseCacheKey(String path, long fingerprint) {
    }

    private record CachedResponse(long expiresAtMillis, String html, CacheState state) {
    }
}
