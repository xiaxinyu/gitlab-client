package com.xiaxinyu.gitlab.client;

import com.xiaxinyu.gitlab.client.api.BaseGitLabApi;
import com.xiaxinyu.gitlab.client.core.GitlabClientProperties;
import lombok.extern.slf4j.Slf4j;
import org.gitlab4j.api.models.Group;
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
import java.util.Objects;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {GitlabClientApplication.class})
public class BaseGitlabApiTest {

    private String groupName;

    private String projectName;

    private String userName;

    @Autowired
    BaseGitLabApi baseGitLabApi;

    @Autowired
    GitlabClientProperties properties;

    @Before
    public void before() throws UnknownHostException {
        groupName = "sz";
        projectName = "ns";
        userName = properties.getUsername();

        InetAddress addr = InetAddress.getLocalHost();
        String ip = addr.getHostAddress();
        log.info("localIp={}", ip);
    }

    @Test
    public void testCreateGroup() throws Exception {
        Group oldGroup = baseGitLabApi.getGroup(groupName);
        if (Objects.nonNull(oldGroup)) {
            log.info("Old group exist: oldGroupId={}, oldGroupName={}", oldGroup.getId(), oldGroup.getName());
            baseGitLabApi.deleteGroup(oldGroup.getId());
            log.info("Delete old group: oldGroupId={}, oldGroupName={}", oldGroup.getId(), oldGroup.getName());
        }

        Group newGroup = baseGitLabApi.createGroup(groupName, groupName, userName);
        Assert.assertNotNull(newGroup);
        log.info("Create new group: newGroupId={}, newGroupName={}", newGroup.getId(), newGroup.getName());
    }

    @Test
    public void testGetProject() throws Exception {
        Project oldProject = baseGitLabApi.getProject(groupName, projectName);
        if (Objects.nonNull(oldProject)) {
            log.info("Old project exist: oldProjectId={}, oldProjectName={}", oldProject.getId(), oldProject.getName());
            baseGitLabApi.deleteProject(oldProject.getId());
            log.info("Delete old project: oldProjectId={}, oldProjectName={}", oldProject.getId(), oldProject.getName());
        }

        Group group = baseGitLabApi.getGroup(groupName);
        if (Objects.isNull(group)) {
            log.error("Not found group: groupName={}", groupName);
        }
        Assert.assertNotNull(group);

        Project newProject = baseGitLabApi.createProject(group.getId(), projectName, userName);
        Assert.assertNotNull(newProject);
        log.info("Create new project: newProjectId={}, newProjectName={}", newProject.getId(), newProject.getName());
    }
}
