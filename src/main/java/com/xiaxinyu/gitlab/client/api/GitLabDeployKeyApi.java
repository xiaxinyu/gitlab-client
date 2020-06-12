package com.xiaxinyu.gitlab.client.api;

import com.xiaxinyu.gitlab.client.api.model.DeployKeyRequest;
import lombok.extern.slf4j.Slf4j;

import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.DeployKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Optional;

/**
 * @author XIAXINYU3
 * GitLab 项目业务API
 */
@Slf4j
@Component
public class GitLabDeployKeyApi {

    @Autowired
    GitLabApi gitLabApi;

    private GitLabApi getGitLabApiAPI(String curUser) throws GitLabApiException {
        gitLabApi.sudo(curUser);
        return gitLabApi;
    }

    public DeployKey addProjectDeployKey(DeployKeyRequest deployKeyRequest) {
        try {
            log.info("创建Gitlab的Deploykey， 请求参数={}", deployKeyRequest.toString());
            DeployKey response = gitLabApi.getDeployKeysApi().addDeployKey(deployKeyRequest.getProjectIdOrPath(), deployKeyRequest.getTitle(), deployKeyRequest.getKey(), deployKeyRequest.getCanPush());
            if (response != null && response.getId() > 0) {
                log.info("成功创建Gitlab的Deploykey， deployKeyId={}", response.getId());
                return response;
            } else {
                log.warn("失败创建Gitlab的Deploykey， projectId={}", deployKeyRequest.getProjectIdOrPath());
            }
        } catch (GitLabApiException e) {
            log.error("添加Gitlab的Deploykey失败，projectId={}", deployKeyRequest.getProjectIdOrPath(), e);
            throw new RuntimeException(String.format("添加Gitlab的Deploykey失败."));
        }
        return null;
    }

    public void deleteAllProjectDeployKey(Integer gitProjectId) {
        try {
            log.info("删除项目Gitlab的所有Deploykey， projectId={}", gitProjectId);
            List<DeployKey> deployKeys = gitLabApi.getDeployKeysApi().getProjectDeployKeys(gitProjectId);
            if (!CollectionUtils.isEmpty(deployKeys)) {
                deployKeys.forEach(deployKey -> deleteProjectDeployKey(gitProjectId, deployKey.getId()));
            }
        } catch (GitLabApiException e) {
            throw new RuntimeException("删除项目Gitlab的所有Deploykey失败，projectId=" + gitProjectId);
        }
    }

    public void deleteProjectDeployKey(Integer gitProjectId, Integer keyId) {
        try {
            log.info("删除项目Gitlab的Deploykey， projectId={}, keyId={}", gitProjectId, keyId);
            gitLabApi.getDeployKeysApi().deleteDeployKey(gitProjectId, keyId);
        } catch (GitLabApiException e) {
            throw new RuntimeException(String.format("删除项目Gitlab的Deploykey失败，projectId=%d, keyId=%d", gitProjectId, keyId));
        }
    }

    public DeployKey getProjectDeployKey(String title, Integer gitProjectId) {
        DeployKey result = null;
        try {
            log.info("查询项目Gitlab的Deploykey， projectId={}, title={}", gitProjectId, title);
            List<DeployKey> deployKeys = gitLabApi.getDeployKeysApi().getProjectDeployKeys(gitProjectId);
            if (!CollectionUtils.isEmpty(deployKeys)) {
                Optional<DeployKey> firstOptional = deployKeys.stream().filter(key -> key.getTitle().equals(title)).findFirst();
                if (firstOptional.isPresent()) {
                    result = firstOptional.get();
                }
            }
        } catch (GitLabApiException e) {
            throw new RuntimeException(String.format("查询项目Gitlab的Deploykey失败，projectId=%d, title=%s", gitProjectId, title));
        }
        return result;
    }

    public void enableDeployKey(String userName, Integer gitProjectId, Integer deployKeyId) throws RuntimeException {
        try {
            getGitLabApiAPI(userName).getDeployKeysApi().enableDeployKey(gitProjectId, deployKeyId);
            log.info("激活DeployKey成功，gitProjectId={}，keyId={}", gitProjectId, deployKeyId);
        } catch (GitLabApiException e) {
            throw new RuntimeException(String.format("激活DeployKey失败，gitProjectId=%d, deployKeyId=%s", gitProjectId, deployKeyId));
        }
    }
}
