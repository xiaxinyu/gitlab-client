package com.xiaxinyu.gitlab.client;

import com.xiaxinyu.gitlab.client.api.BaseGitLabApi;
import com.xiaxinyu.gitlab.client.core.GitlabClientProperties;
import lombok.extern.slf4j.Slf4j;
import org.gitlab4j.api.models.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.CollectionUtils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.List;
import java.util.Objects;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {GitlabClientApplication.class})
public class BaseGitlabApiTest {

    private String groupName;

    private String projectName;

    private String branchRef;

    private String branchName;

    private String tagName;

    private String userName;

    @Autowired
    BaseGitLabApi baseGitLabApi;

    @Autowired
    GitlabClientProperties properties;

    @Before
    public void before() throws Exception {
        groupName = "sz";
        projectName = "ns";
        branchRef = "master";
        branchName = "fdc";
        tagName = "tag-fdc";
        userName = properties.getUsername();

        InetAddress addr = InetAddress.getLocalHost();
        String ip = addr.getHostAddress();
        log.info("localIp={}", ip);


        NetworkInterface network = NetworkInterface.getByInetAddress(addr);

        byte[] mac = network.getHardwareAddress();

        System.out.print("Current MAC address : ");

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mac.length; i++) {
            sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? ":" : ""));
        }
        System.out.println(sb.toString());
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
    public void testCreateProject() throws Exception {
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

    @Test
    public void testCreateBranch() throws Exception {
        Project project = baseGitLabApi.getProject(groupName, projectName);
        Assert.assertNotNull(project);

        List<Branch> branches = baseGitLabApi.getBranchs(project.getId());
        if (!CollectionUtils.isEmpty(branches)) {
            log.info("Old branches exist: oldProjectId={}, oldProjectName={}, branchSize={}", project.getId(), project.getName(), branches.size());
            for (Branch branch : branches) {
                if (branch.getName().indexOf(branchRef) < 0) {
                    baseGitLabApi.deleteBranch(project.getId(), branch.getName(), userName);
                    log.info("Delete old branch: oldBranchName={}", branch.getName());
                }
            }
        }

        Branch newBranch = baseGitLabApi.createBranch(project.getId(), branchName, branchRef);
        Assert.assertNotNull(newBranch);
        log.info("Create new branch: newBranchName={}", newBranch.getName());
    }

    @Test
    public void testCreateTag() throws Exception {
        Project project = baseGitLabApi.getProject(groupName, projectName);
        Assert.assertNotNull(project);

        Tag oldTag = baseGitLabApi.getTagByName(project.getId(), tagName);
        if (Objects.nonNull(oldTag)) {
            log.info("Old tag exist: oldTagName={}", oldTag.getName());
            baseGitLabApi.deleteTag(project.getId(), tagName, userName);
            log.info("Delete old tag: oldTagName={}", tagName);
        }

        Tag tag = baseGitLabApi.createTag(project.getId(), tagName, branchRef, "test", "test", userName);
        Assert.assertNotNull(tag);
        log.info("Create new tag: newTagName={}", tag.getName());
    }

    @Test
    public void testCommit() throws Exception {
        Project project = baseGitLabApi.getProject("t25001-test-obj", "test-door");
        Assert.assertNotNull(project);
        log.info("projectId={}", project.getId());

        Commit commit = baseGitLabApi.getCommit(project.getId(), "88e7ffca9bd15649572667475121e1c73c3eb824", "aliyate");
        Assert.assertNotNull(commit);
        log.info("commit={}", commit.getTitle());
    }
}
