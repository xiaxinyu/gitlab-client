package com.xiaxinyu.gitlab.client.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 配置
 * @author XIAXINYU3
 * @date 2020.6.12
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Component
@ConfigurationProperties(prefix = "gitlab")
public class GitlabClientProperties {
    private boolean fastJsonAutoDiscover = false;
    private String address;
    private String username;
    private String token;
}
