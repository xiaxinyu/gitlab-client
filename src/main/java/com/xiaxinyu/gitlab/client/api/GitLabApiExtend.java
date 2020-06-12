package com.xiaxinyu.gitlab.client.api;

import org.apache.commons.lang.StringUtils;
import org.gitlab4j.api.Constants;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiClient;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.net.URL;
import java.util.Map;
import java.util.Objects;

/**
 * Gitlab扩展API
 *
 * @author XIAXINYU3
 * @date 2019.9.3
 */
public class GitLabApiExtend extends GitLabApiClient {

    public GitLabApiExtend(GitLabApi.ApiVersion apiVersion, String hostUrl, Constants.TokenType tokenType, String authToken, String secretToken, Map<String, Object> clientConfigProperties) {
        super(apiVersion, hostUrl, tokenType, authToken, secretToken, clientConfigProperties);
    }

    public String getPlainText(MultivaluedMap<String, String> queryParams, String url) {
        try {
            Response response = getWithAccepts(queryParams, new URL(url), MediaType.TEXT_PLAIN);
            if (Objects.isNull(response)) {
                return StringUtils.EMPTY;
            }
            if (response.getStatus() != Response.Status.OK.getStatusCode()) {
                throw new RuntimeException(String.format("获取Git数据出现错误：statusCode=%d", response.getStatus()));
            }
            return response.readEntity(String.class);
        } catch (Exception e) {
            throw new RuntimeException(String.format("获取Git数据出现错误：%s", e.getMessage()));
        }
    }
}
