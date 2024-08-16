package com.neutroware.ebaysyncserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;


@Configuration
public class AsyncConfig {

    //This will be the default executor since only one is defined
    //So methods annotated @Async without any arguments will use this
    //If need to have another executor, another bean must be defined
    @Bean(name = "singleThreadExecutor")
    public Executor singleThreadExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);  // Only one thread in the pool
        executor.setMaxPoolSize(1);   // Maximum one thread
        executor.setQueueCapacity(100); // Queue capacity for holding pending tasks
        executor.setThreadNamePrefix("SingleThreadExecutor-");
        executor.initialize();
        return executor;
    }
}
