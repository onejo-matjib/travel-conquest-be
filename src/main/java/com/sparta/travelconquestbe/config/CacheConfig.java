package com.sparta.travelconquestbe.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {
    @Bean
    // TTL 사용을 위한 외부 캐시 라이브러리 사용 'Caffeine'
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("searchPostsCache","searchPostsBestCache");
        cacheManager.setCaffeine(
                Caffeine.newBuilder()
                        // 캐시 TTL 설정
                        .expireAfterWrite(10, TimeUnit.MINUTES)
                        // 최대 캐시 엔트리 수
                        .maximumSize(1000)
        );
        return cacheManager;
    }
}
