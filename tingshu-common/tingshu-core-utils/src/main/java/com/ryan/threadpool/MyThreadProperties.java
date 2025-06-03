package com.ryan.threadpool;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "thread.pool")
public class MyThreadProperties {
    public Integer corePoolSize = 16;
    public Integer maximumPoolSize = 32;
    public Integer keepAliveTime = 50;
    public Integer queueLength = 100;
}
