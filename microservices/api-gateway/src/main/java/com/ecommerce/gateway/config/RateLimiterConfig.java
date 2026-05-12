package com.ecommerce.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
public class RateLimiterConfig {

    /**
     * Rate limit key: client IP adresi
     * Her IP için ayrı sayaç tutulur
     */
    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            String ip = exchange.getRequest().getRemoteAddress() != null
                    ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                    : "unknown";
            return Mono.just(ip);
        };
    }

    /**
     * Genel API rate limiter: 100 istek/dakika
     * replenishRate: saniyede eklenen token sayısı (100/60 ≈ 2)
     * burstCapacity: anlık max istek sayısı
     */
    @Bean
    public RedisRateLimiter defaultRateLimiter() {
        return new RedisRateLimiter(10, 20, 1);
        // replenishRate=10 (saniyede 10 token), burstCapacity=20, requestedTokens=1
    }

    /**
     * Auth endpoint rate limiter: 5 istek/dakika (brute force koruması)
     */
    @Bean
    public RedisRateLimiter authRateLimiter() {
        return new RedisRateLimiter(2, 5, 1);
        // replenishRate=2 (saniyede 2 token), burstCapacity=5
    }
}
