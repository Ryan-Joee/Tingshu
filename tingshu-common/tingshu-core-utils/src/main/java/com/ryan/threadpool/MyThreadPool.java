package com.ryan.threadpool;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

@EnableConfigurationProperties(MyThreadProperties.class)
@Configuration
public class MyThreadPool {
    @Autowired
    private MyThreadProperties threadProperties;
    /**
     *  LinkedBlockingQueue
     *  不会引起空间碎片问题
     */
    @Bean
    public ThreadPoolExecutor myPoolExecutor() {
        return new ThreadPoolExecutor(threadProperties.getCorePoolSize(),
                threadProperties.getMaximumPoolSize(),
                threadProperties.getKeepAliveTime(),
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(threadProperties.getQueueLength()),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.CallerRunsPolicy());
    }
}
