package edu.example.springmvcdemo.rate_limiting;

import com.giffing.bucket4j.spring.boot.starter.config.cache.AbstractCacheResolverTemplate;
import com.giffing.bucket4j.spring.boot.starter.config.cache.SyncCacheResolver;
import io.github.bucket4j.distributed.proxy.AbstractProxyManager;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CustomCacheResolver extends AbstractCacheResolverTemplate<String> implements SyncCacheResolver {

    private final AbstractProxyManager<String> proxyManager;

    @Override
    public String castStringToCacheKey(String key) {
        return key;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public AbstractProxyManager<String> getProxyManager(String cacheName) {
        return this.proxyManager;
    }
}