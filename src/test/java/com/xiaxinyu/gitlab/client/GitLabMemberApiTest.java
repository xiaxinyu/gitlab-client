package com.xiaxinyu.gitlab.client;

import com.xiaxinyu.gitlab.client.api.BaseGitLabApi;
import com.xiaxinyu.gitlab.client.api.GitLabMemberApi;
import lombok.extern.slf4j.Slf4j;
import org.gitlab4j.api.models.AccessLevel;
import org.gitlab4j.api.models.Member;
import org.gitlab4j.api.models.Project;
import org.gitlab4j.api.models.User;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Objects;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {GitlabClientApplication.class})
public class GitLabMemberApiTest {
    private String groupName;

    private String projectName;

    private String userName;

    @Autowired
    BaseGitLabApi baseGitLabApi;

    @Autowired
    GitLabMemberApi gitLabMemberApi;

    @Before
    public void before() {
        groupName = "sz";
        projectName = "ns";
        this.userName = "summer";
    }

    @Test
    public void testUser() {
        User user = gitLabMemberApi.getUser(this.userName);
        if (Objects.nonNull(user)) {
            log.info("用户存在，删除用户: user={}", this.userName);
            gitLabMemberApi.deleteUser(user.getId());
        }
        log.info("创建用户：user={}", this.userName);
        gitLabMemberApi.createUser(this.userName, this.userName, this.userName + "@126.com", "King123456");
    }

    @Test
    public void testProjectUser() throws Exception {
        Project project = baseGitLabApi.getProject(groupName, projectName);
        Assert.assertNotNull(project);

        User user = gitLabMemberApi.getUser(this.userName);
        Assert.assertNotNull(user);

        Member member = gitLabMemberApi.addProjectMember(project.getId(), user.getId(), AccessLevel.MAINTAINER);
        Assert.assertNotNull(member);
    }
}
