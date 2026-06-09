package com.system_gestion_soutenance.api.common.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
@SuppressWarnings("PMD")

@Configuration
@EnableCaching
@EnableAsync
public class AsyncCacheConfig {
}