package com.xiaxinyu.gitlab.client;


import com.xiaxinyu.gitlab.client.api.BaseGitLabApi;
import com.xiaxinyu.gitlab.client.api.GitLabDeployKeyApi;
import com.xiaxinyu.gitlab.client.api.model.DeployKeyRequest;
import com.xiaxinyu.gitlab.client.core.GitlabClientProperties;
import com.xiaxinyu.gitlab.client.utils.JSchUtils;
import lombok.extern.slf4j.Slf4j;
import org.gitlab4j.api.models.DeployKey;
import org.gitlab4j.api.models.Project;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Objects;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {GitlabClientApplication.class})
public class GitLabDeployKeyApiTest {
    private String userName;

    private String deployKeyName;

    private String projectName;

    private String groupName;

    @Autowired
    GitlabClientProperties properties;

    @Autowired
    BaseGitLabApi baseGitLabApi;

    @Autowired
    GitLabDeployKeyApi gitLabDeployKeyApi;


    @Before
    public void before() throws UnknownHostException {
        userName = properties.getUsername();
        deployKeyName = "test";
        projectName = "ns";
        groupName = "sz";

        InetAddress addr = InetAddress.getLocalHost();
        String ip = addr.getHostAddress();
        log.info("localIp={}", ip);
    }

    @Test
    public void testCreateGroup() throws Exception {
        Project project = baseGitLabApi.getProject(groupName, projectName);
        Assert.assertNotNull(project);

        DeployKey deployKey = gitLabDeployKeyApi.getProjectDeployKey(deployKeyName, project.getId());
        if (Objects.nonNull(deployKey)) {
            log.info("Old DeployKey exist: oldDeployKeyId={}, oldDeployKeyTitle={}", deployKey.getId(), deployKey.getTitle());
            gitLabDeployKeyApi.deleteProjectDeployKey(project.getId(), deployKey.getId());
            log.info("Delete old DeployKey: oldDeployKeyId={}, oldDeployKeyTitle={}", deployKey.getId(), deployKey.getTitle());
        }


        Map<String, String> keys = JSchUtils.genKeyPair(deployKeyName);
        Assert.assertNotNull(keys);
        Assert.assertTrue(keys.containsKey(JSchUtils.PUBLIC_KEY));

        DeployKeyRequest deployKeyRequest = DeployKeyRequest.builder()
                .key(keys.get(JSchUtils.PUBLIC_KEY)).projectIdOrPath(project.getId())
                .title(deployKeyName).canPush(Boolean.TRUE).build();
        DeployKey newDeployKey = gitLabDeployKeyApi.addProjectDeployKey(deployKeyRequest);
        Assert.assertNotNull(newDeployKey);
        log.info("Create new deployKey: newDeployKeyId={}, newDeployKeyTitle={}", newDeployKey.getId(), newDeployKey.getTitle());
    }
}
