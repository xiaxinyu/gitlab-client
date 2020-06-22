package com.xiaxinyu.gitlab.client.api;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.GroupApi;
import org.gitlab4j.api.ProjectApi;
import org.gitlab4j.api.models.AccessLevel;
import org.gitlab4j.api.models.Member;
import org.gitlab4j.api.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Gitlab 连接器
 *
 * @author XIAXINYU3
 * @date 2020.6.12
 */
@Slf4j
@Component
public class GitLabMemberApi {

    @Autowired
    GitLabApi gitLabApi;

    private GitLabApi getGitLabApi(String curUser) throws GitLabApiException {
        gitLabApi.sudo(curUser);
        return gitLabApi;
    }

    /**
     * 获取组成员
     *
     * @param groupId git组ID
     * @return
     * @throws Exception
     */
    public List<Member> getGroupMembers(Integer groupId) {
        List<Member> lsMem = null;
        try {
            lsMem = gitLabApi.getGroupApi().getMembers(groupId);
        } catch (GitLabApiException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("获取GitLab组成员信息异常：" + e.getMessage());
        }
        return lsMem;
    }

    /**
     * 判断用户有哪些访问级别
     *
     * @param user
     * @param accessLevel
     * @return
     * @throws Exception
     */
    public boolean groupMemberHasRole(Integer groupId, String user, AccessLevel[] accessLevel) throws Exception {
        List<Member> array = getGroupMembers(groupId);
        List<AccessLevel> lsAccessLevel = Arrays.asList(accessLevel);
        boolean flag = memberContainsUser(array, lsAccessLevel, user);
        return flag;
    }

    private boolean memberContainsUser(List<Member> array, List<AccessLevel> lsAccessLevel, String user) {
        boolean flag = false;
        for (Member member : array) {
            if (user.equals(member.getUsername()) && lsAccessLevel.contains(member.getAccessLevel())) {
                flag = true;
                break;
            }
        }
        return flag;
    }

    public boolean projectMemberHasRole(Integer id, String user, AccessLevel[] accessLevel) throws Exception {
        JSONObject temp = new JSONObject();
        temp.put("id", id);
        List<Member> lsMem = getProjectMembers(id);
        List<AccessLevel> lsAccessLevel = Arrays.asList(accessLevel);
        boolean flag = memberContainsUser(lsMem, lsAccessLevel, user);
        return flag;
    }


    public List<Member> queryGroupMembers(Integer groupId) throws GitLabApiException {
        // 需要查询的组id不能为空
        List<Member> groupMembers = gitLabApi.getGroupApi().getMembers(groupId);
        return groupMembers;
    }


    public User getGitUser(String user) {
        try {
            User gitUser = gitLabApi.getUserApi().getUser(user);
            return gitUser;
        } catch (GitLabApiException ex) {
            if (ex.getHttpStatus() != 404 && !"the specified username was not found".equals(ex.getMessage())) {
                throw new RuntimeException(ex);
            }
            log.error("查询不到git用户:" + user, ex);
            throw new RuntimeException("查询不到git用户:" + user, ex);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * 根据用户名称，获取gitlab用户信息
     *
     * @param user 用户名
     * @return Gitlab用户
     */
    public User getUser(String user) {
        try {
            log.debug("查询gitlab用户：user={}", user);
            User gitUser = gitLabApi.getUserApi().getUser(user);
            return gitUser;
        } catch (GitLabApiException ex) {
            if (ex.getHttpStatus() != 404 && !"the specified username was not found".equals(ex.getMessage())) {
                throw new RuntimeException(ex.getMessage());
            }
            log.error("查询不到git用户: {}", user, ex);
            throw new RuntimeException(String.format("查询不到git用户: %s", user));
        } catch (Exception ex) {
            throw new RuntimeException(String.format("查询Git用户出现错误：%s", ex.getMessage()));
        }
    }

    public User createUser(String user, String name, String email, String password) {
        try {
            User u = new User();
            u.setUsername(user);
            u.setEmail(email);
            u.setName(name);
            return gitLabApi.getUserApi().createUser(u, password, false);
        } catch (GitLabApiException e) {
            throw new RuntimeException(String.format("创建Git用户出现错误：%s", e.getMessage()));
        }
    }

    public void deleteUser(Integer userId) {
        try {
            gitLabApi.getUserApi().deleteUser(userId);
        } catch (GitLabApiException e) {
            throw new RuntimeException(String.format("删除Git用户出现错误：%s", e.getMessage()));
        }
    }

    public Member addGroupMembers(Integer groupId, Integer userId, AccessLevel accessLevel) throws Exception {
        // 校验用户是否已经存在组中
        GroupApi groupApi = gitLabApi.getGroupApi();
        if (hasGroupMember(groupId, userId)) {
            return groupApi.updateMember(groupId, userId, accessLevel);

        } else {
            return groupApi.addMember(groupId, userId, accessLevel);
        }
    }

    public void removeMember(Integer groupId, Integer userId) throws Exception {
        gitLabApi.getGroupApi().removeMember(groupId, userId);
    }

    /**
     * 获取工程组成员
     *
     * @param gitProjectId git项目ID
     * @return
     * @throws Exception
     */
    public List<Member> getProjectMembers(Integer gitProjectId) {
        List<Member> lsMem;
        try {
            lsMem = gitLabApi.getProjectApi().getMembers(gitProjectId);
        } catch (GitLabApiException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(String.format("获取GitLab项目成员信息异常：%s", e.getMessage()));
        }
        return lsMem;
    }

    /**
     * 将用户添加到项目中
     *
     * @param projectId
     * @param userId
     * @param accessLevel
     * @throws Exception
     */
    public Member addProjectMember(Integer projectId, Integer userId, AccessLevel accessLevel) throws Exception {
        ProjectApi projectApi = gitLabApi.getProjectApi();
        if (hasProjectMember(projectId, userId)) {
            return projectApi.updateMember(projectId, userId, accessLevel);
        } else {
            return projectApi.addMember(projectId, userId, accessLevel);
        }
    }

    /**
     * 将用户从项目中移除
     *
     * @param projectId
     * @param userId
     * @throws Exception
     */
    public void removeProjectMember(Integer projectId, Integer userId) throws Exception {
        gitLabApi.getProjectApi().removeMember(projectId, userId);
    }

    /**
     * 判断用户是否在组里面
     *
     * @param groupId
     * @param userId
     * @return
     * @throws GitLabApiException
     */
    public boolean hasGroupMember(Integer groupId, Integer userId) throws GitLabApiException {
        boolean flag = false;
        GroupApi groupApi = gitLabApi.getGroupApi();
        List<Member> lsM = groupApi.getMembers(groupId);
        for (Member m : lsM) {
            if (m.getId().equals(userId)) {
                flag = true;
                break;
            }
        }
        return flag;
    }

    /**
     * 判断用户是否在工程里面
     *
     * @param projectId
     * @param userId
     * @return
     * @throws GitLabApiException
     */
    public boolean hasProjectMember(Integer projectId, Integer userId) throws GitLabApiException {
        boolean flag = false;
        ProjectApi projectApi = gitLabApi.getProjectApi();
        List<Member> lsM = projectApi.getMembers(projectId);
        for (Member m : lsM) {
            if (m.getId().equals(userId)) {
                flag = true;
                break;
            }
        }
        return flag;
    }
}
