package edu.example.springmvcdemo.rate_limiting;

import com.giffing.bucket4j.spring.boot.starter.config.cache.SyncCacheResolver;
import io.github.bucket4j.distributed.jdbc.BucketTableSettings;
import io.github.bucket4j.distributed.jdbc.PrimaryKeyMapper;
import io.github.bucket4j.distributed.jdbc.SQLProxyConfiguration;
import io.github.bucket4j.distributed.proxy.AbstractProxyManager;
import io.github.bucket4j.postgresql.PostgreSQLadvisoryLockBasedProxyManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class Config {

    @Bean
    public AbstractProxyManager<String> getBucket4jProxyManager(DataSource dataSource) {
        var configuration = SQLProxyConfiguration.builder()
                .withTableSettings(BucketTableSettings.customSettings(
                        "rate_limiting_cache",
                        "key",
                        "state")
                )
                .withPrimaryKeyMapper(PrimaryKeyMapper.STRING)
                .build(dataSource);

        return new PostgreSQLadvisoryLockBasedProxyManager<>(configuration);
    }

    @Bean
    public SyncCacheResolver bucket4jCacheResolver(AbstractProxyManager<String> manager) {
        return new CustomCacheResolver(manager);
    }
}
