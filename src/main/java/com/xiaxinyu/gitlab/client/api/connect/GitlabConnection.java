package com.xiaxinyu.gitlab.client.api.connect;

import com.alibaba.fastjson.support.jaxrs.FastJsonAutoDiscoverable;

import com.xiaxinyu.gitlab.client.api.GitLabApiExtend;
import com.xiaxinyu.gitlab.client.core.GitlabClientProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.gitlab4j.api.Constants;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApi.ApiVersion;
import org.gitlab4j.api.GitLabApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Gitlab 连接器
 *
 * @author XIAXINYU3
 * @date 2020.6.12
 */
@Slf4j
@Configuration
public class GitlabConnection {

    @Autowired
    GitlabClientProperties config;

    @Bean
    public GitLabApi getGitLabApi() throws GitLabApiException {
        // 默认禁用Fast作为json解析的Provider，会跟最新的gitlab接口冲突
        FastJsonAutoDiscoverable.autoDiscover = config.isFastJsonAutoDiscover();

        log.debug("创建新的gitlab client api...，token={}", config.getToken());
        GitLabApi gitLabApi = new GitLabApi(ApiVersion.V4, config.getAddress(), config.getToken());

        // 默认不使用sudo操作，有需要再设置sudo
        gitLabApi.sudo(null);

        return gitLabApi;
    }

    @Bean
    public GitLabApiExtend getGitLabApiClientExtend() {
        return new GitLabApiExtend(ApiVersion.V4, config.getAddress(), Constants.TokenType.PRIVATE, config.getToken(), null, null);
    }
}
