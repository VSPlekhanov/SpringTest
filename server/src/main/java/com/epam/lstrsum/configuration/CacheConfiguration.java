package com.epam.lstrsum.configuration;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
@EnableCaching
public class CacheConfiguration {
    @Bean
    public CacheManager cacheManager(net.sf.ehcache.CacheManager internalCacheManager) {
        return new EhCacheCacheManager(internalCacheManager);
    }

    @Bean
    public net.sf.ehcache.CacheManager internalCacheManager() {
        return ehCacheCacheManager().getObject();
    }

    @Bean
    public EhCacheManagerFactoryBean ehCacheCacheManager() {
        EhCacheManagerFactoryBean factory = new EhCacheManagerFactoryBean();
        factory.setConfigLocation(new ClassPathResource("ehcache.xml"));
        factory.setShared(true);
        return factory;
    }
}
