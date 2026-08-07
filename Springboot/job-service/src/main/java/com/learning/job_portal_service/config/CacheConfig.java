package com.learning.job_portal_service.config;

import java.time.Duration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.SocketOptions;

/**
 * Cache configuration.
 *
 * Two profiles:
 *  - spring.cache.type=redis  → full Redis-backed cache with 3 named TTLs
 *  - spring.cache.type=none   → in-memory ConcurrentMap (for local dev without Redis)
 *
 * The Lettuce connection is configured to NOT validate on connect so the
 * application starts even when Redis is not yet available. Commands will fail
 * at invocation time rather than at startup.
 *
 * Cache names and TTLs:
 *   job          — single job detail       — 10 min
 *   jobs-search  — paginated search        —  2 min
 *   jobs-mine    — recruiter's own jobs    —  5 min
 */
@Configuration
@EnableCaching
public class CacheConfig {

    // ── Redis-backed cache (active when spring.cache.type=redis) ─────────────

    @Bean
    @ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis")
    public LettuceConnectionFactory redisConnectionFactory(
            org.springframework.boot.autoconfigure.data.redis.RedisProperties props) {

        // Never block at startup: use async connect + no socket-level timeout on
        // the initial handshake so the app boots even if Redis is temporarily down.
        SocketOptions socketOptions = SocketOptions.builder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();

        ClientOptions clientOptions = ClientOptions.builder()
                .socketOptions(socketOptions)
                .disconnectedBehavior(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS)
                .autoReconnect(true)
                .build();

        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
                .clientOptions(clientOptions)
                .commandTimeout(Duration.ofSeconds(2))
                .build();

        org.springframework.data.redis.connection.RedisStandaloneConfiguration serverConfig =
                new org.springframework.data.redis.connection.RedisStandaloneConfiguration(
                        props.getHost(), props.getPort());

        LettuceConnectionFactory factory = new LettuceConnectionFactory(serverConfig, clientConfig);
        factory.setValidateConnection(false); // don't ping on borrow — avoids startup block
        factory.setEagerInitialization(false); // lazy: connect on first use
        return factory;
    }

    @Bean
    @ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis")
    public CacheManager redisCacheManager(RedisConnectionFactory factory) {
        RedisCacheConfiguration base = RedisCacheConfiguration.defaultCacheConfig()
                .disableCachingNullValues()
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(new GenericJackson2JsonRedisSerializer()));

        return RedisCacheManager.builder(factory)
                .withCacheConfiguration("job",
                        base.entryTtl(Duration.ofMinutes(10)))
                .withCacheConfiguration("jobs-search",
                        base.entryTtl(Duration.ofMinutes(2)))
                .withCacheConfiguration("jobs-mine",
                        base.entryTtl(Duration.ofMinutes(5)))
                .build();
    }

    // ── In-memory fallback (active when spring.cache.type=none) ──────────────

    @Bean
    @ConditionalOnProperty(name = "spring.cache.type", havingValue = "none")
    public CacheManager noOpCacheManager() {
        // ConcurrentMapCacheManager provides real caching without Redis.
        // Useful for local development when Redis is not installed.
        return new ConcurrentMapCacheManager("job", "jobs-search", "jobs-mine");
    }
}
