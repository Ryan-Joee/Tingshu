package com.ryan.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "wechat.login")
@Data
@Component
public class WeChatProperties {
    private String appId;
    private String appSecret;
}
