package com.xiaxinyu.gitlab.client.api;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.GroupApi;
import org.gitlab4j.api.ProjectApi;
import org.gitlab4j.api.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.naming.AuthenticationException;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
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
public class BaseGitLabApi {

    @Autowired
    GitLabApi gitLabApi;

    private GitLabApi getGitLabApi(String curUser) throws GitLabApiException {
        gitLabApi.sudo(curUser);
        return gitLabApi;
    }

    /**
     * 获取项目分组
     *
     * @param projectId
     * @return
     */
    public List<Branch> getBranchs(Integer projectId) {
        List<Branch> lsB;
        try {
            lsB = gitLabApi.getRepositoryApi().getBranches(projectId);
            return lsB;
        } catch (GitLabApiException e) {
            log.error(e.getMessage(), e);
            // TODO Auto-generated catch block
            if (e.getHttpStatus() != 404) {
                throw new RuntimeException("获取项目分支错误,请联系管理员!error:" + e.getMessage());
            }
            throw new RuntimeException("获取项目分支错误，无效的git项目id:" + projectId);
        }

    }

    /**
     * 获取项目tag
     *
     * @param projectId
     * @return
     */
    public List<Tag> getTags(Integer projectId) {
        try {
            List<Tag> lsT = gitLabApi.getTagsApi().getTags(projectId);

            return lsT;
        } catch (GitLabApiException e) {
            log.error(e.getMessage(), e);
            if (e.getHttpStatus() != 404) {
                throw new RuntimeException("获取项目Tag错误,请联系管理员!error:" + e.getMessage());
            }
            throw new RuntimeException("获取项目Tag错误，无效的git项目id:" + projectId);
        }

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

    public Group getGroup(String groupPath) throws AuthenticationException, IOException, GitLabApiException {
        // 参数必须包含name（组名称）属性,其它为可选参数。
        Group group = null;
        try {
            group = gitLabApi.getGroupApi().getGroup(groupPath);
        } catch (GitLabApiException ex) {
            if (ex.getHttpStatus() != 404) {
                throw ex;
            }
        }
        return group;
    }

    public Group createGroup(JSONObject param, String userName) throws Exception {
        String name = param.getString("name").trim();
        String path = param.containsKey("path") ? param.getString("path") : name;
        return getGitLabApi(userName).getGroupApi().addGroup(name, path);
    }

    public boolean deleteGroup(Integer groupId) throws Exception {
        gitLabApi.getGroupApi().deleteGroup(groupId);
        return true;
    }


    public Project createProject(JSONObject param, String userName) throws Exception {
        // 设置参数
        String name = param.getString("name");
        Integer namespaceId = param.containsKey("namespaceId") ? param.getInteger("namespaceId") : null;

        Project project = getGitLabApi(userName).getProjectApi().createProject(namespaceId, name);

        return project;
    }

    public boolean deleteProject(Integer projectId) throws Exception {
        // 需要删除的组的工程的id不能为空
        try {
            gitLabApi.getProjectApi().deleteProject(projectId);
        } catch (GitLabApiException ex) {
            if (ex.getHttpStatus() != 404) {
                log.error(ex.getMessage(), ex);
                throw new RuntimeException("删除gitlab项目(id=" + projectId + ")异常,请联系管理员！error:" + ex.getMessage());
            }
        }
        return true;
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
            if (Objects.isNull(gitUser)) {
                throw new RuntimeException(String.format("查询不到git用户: %s", user));
            }
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

    public Project getProject(Integer projectId) throws Exception {
        log.info("获取Git项目，请求参数: projectId={}", projectId);

        try {
            return gitLabApi.getProjectApi().getProject(projectId);
        } catch (GitLabApiException ex) {
            // 如果没有则返回null
            if (ex.getHttpStatus() != 404) {
                throw ex;
            }
            throw ex;
        }
    }


    public Project getProject(String namespace, String projectCode) throws Exception {
        log.info("获取Git项目，请求参数: namespace={}, projectCode={}", namespace, projectCode);

        try {
            // 如果没有工程ID则根据，namespace以及projectCode进行查找
            return gitLabApi.getProjectApi().getProject(namespace, projectCode);
        } catch (GitLabApiException ex) {
            // 如果没有则返回null
            if (ex.getHttpStatus() != 404) {
                throw ex;
            }
            throw ex;
        }
    }

    public SshKey createSSH(Integer userId, String title, String key) throws Exception {
        SshKey sshKey = gitLabApi.getUserApi().addSshKey(userId, title, key);
        return sshKey;
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

    public List<Group> getGroups() throws GitLabApiException {
        return gitLabApi.getGroupApi().getGroups();
    }

    /**
     * 获取组下面的工程
     *
     * @param groupId
     * @return
     * @throws GitLabApiException
     */
    public List<Project> getProjects(Integer groupId) throws GitLabApiException {
        return gitLabApi.getGroupApi().getProjects(groupId);
    }

    /**
     * 获取git仓库文件
     *
     * @param projectIdOrPath
     * @param filePath
     * @param ref
     * @return
     * @throws GitLabApiException
     */
    public RepositoryFile getFile(Object projectIdOrPath, String filePath, String ref)
            throws GitLabApiException {
        return gitLabApi.getRepositoryFileApi().getFile(projectIdOrPath, filePath, ref);
    }

    /**
     * 上传文件
     *
     * @param idOrPath
     * @param file
     * @param branchName
     * @param commitMessage
     * @param userName
     * @throws GitLabApiException
     */
    public void createNewFile(Object idOrPath, RepositoryFile file, String branchName, String commitMessage, String userName) {
        try {
            getGitLabApi(userName).getRepositoryFileApi().createFile(idOrPath, file, branchName, commitMessage);
        } catch (GitLabApiException e) {
            throw new RuntimeException("创建文件到Gitlab出现错误");
        }
    }

    /**
     * 更新git仓库文件
     *
     * @param file
     * @param projectId
     * @param branchName
     * @param commitMessage
     * @return
     * @throws GitLabApiException
     */
    public RepositoryFile updateFile(RepositoryFile file, Integer projectId, String branchName,
                                     String commitMessage) throws GitLabApiException {
        return gitLabApi.getRepositoryFileApi().updateFile(file, projectId, branchName, commitMessage);
    }

    /**
     * 添加 文件到git仓库
     *
     * @param file
     * @param projectId
     * @param branchName
     * @param commitMessage
     * @return
     * @throws GitLabApiException
     */
    public RepositoryFile createFile(RepositoryFile file, Integer projectId, String branchName,
                                     String commitMessage) throws GitLabApiException {
        return gitLabApi.getRepositoryFileApi().createFile(file, projectId, branchName, commitMessage);
    }

    /**
     * 创建分支，此功能可以初始化project（即添加readme功能），例如（id,"developer","master"）
     */
    public Branch createBranch(Integer projectId, String branchName, String ref) throws GitLabApiException {
        return gitLabApi.getRepositoryApi().createBranch(projectId, branchName, ref);
    }

    /**
     * 获取提交记录
     *
     * @param projectId
     * @param branch
     * @param startDate
     * @param endDate
     * @return
     */
    public Integer getCommitsCount(Integer projectId, String branch, Date startDate, Date endDate) {
        try {
            branch = branch == null || branch.trim().equals("") ? "master" : branch;
            List<Commit> lsCommits = gitLabApi.getCommitsApi().getCommits(projectId, branch, startDate, endDate);
            return lsCommits.size();
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            throw new RuntimeException("获取git提交记录总数异常，请联系管理员！error:" + ex.getMessage());
        }
    }

    /**
     * 获取提交记录
     *
     * @param projectId 工程ID
     * @param branch    分支
     * @param total     记录总大小
     * @param curPage   当前页
     * @param pageSize  每页大小
     * @return
     * @throws GitLabApiException
     */
    public JSONObject getCommits(Integer projectId, String branch, int total, int curPage, int pageSize) {
        try {
            JSONObject ret = new JSONObject();
            branch = branch == null || branch.trim().equals("") ? "master" : branch;
            pageSize = pageSize == 0 ? 1 : pageSize;
            curPage = curPage == 0 ? 1 : curPage;

            ret.put("curPage", curPage);
            ret.put("pageSize", pageSize);

            List<Commit> lsCommit = gitLabApi.getCommitsApi().getCommits(projectId, branch, null, null, null,
                    (curPage - 1), pageSize);
            ret.put("list", JSON.toJSON(lsCommit));
            return ret;
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            throw new RuntimeException("获取git提交记录异常，请联系管理员！error:" + ex.getMessage());
        }
    }

    public Commit getTheLastCommit(Integer projectId, String branch) {
        try {
            branch = (StringUtils.isBlank(branch) ? "master" : branch);
            List<Commit> lsCommit = gitLabApi.getCommitsApi().getCommits(projectId, branch, null, null, null, 1, 1);
            if (CollectionUtils.isEmpty(lsCommit)) {
                return null;
            }
            return lsCommit.get(0);
        } catch (Exception e) {
            log.error("获取git提交记录异常", e);
            throw new RuntimeException(String.format("获取git提交记录异常 :  %s", e.getMessage()));
        }
    }

    public void updateGitlabUserForLDAP(String username, String ldapBaseDN, String ldapProvider) {
        try {
            String userDN = "uid=" + username + "," + ldapBaseDN;
            log.debug("设置Gitlab用户{}的LDAP属性：{} {}", username, userDN, ldapProvider);

            User user = gitLabApi.getUserApi().getUser(username);
            log.debug("查询用户信息：{}", JSON.toJSONString(user));

            if (user != null) {
                user.setExternUid(userDN);
                user.setProvider(ldapProvider);
                //此处密码针对LDAP的无效，所以设置任何值都可以
                gitLabApi.getUserApi().updateUser(user, "Devops123");
                log.debug("设置Gitlab用户{}成功！", username);
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            throw new RuntimeException("设置gitlab用户LDAP属性异常！error:" + ex.getMessage());
        }
    }

    /**
     * 转换项目
     *
     * @param idOrPath  待转换ID或者路径
     * @param namespace 命名空间
     * @param userName  用户名
     */
    public Project transferProject(Object idOrPath, String namespace, String userName) {
        Project project = null;
        try {
            project = getGitLabApi(userName).getProjectApi().transferProject(idOrPath, namespace);
        } catch (Exception e) {
            log.error("从GIT仓库导入项目发生异常: ", e);
            throw new RuntimeException("从GIT仓库导入项目发生异常: " + e.getMessage());
        }
        return project;
    }

    /**
     * 查询项目
     *
     * @param idOrPath ID或者路径
     * @return Gitlab项目信息
     */
    public Project getProject(Object idOrPath) {
        Project project = null;
        try {
            project = gitLabApi.getProjectApi().getProject(idOrPath);
        } catch (Exception e) {
            log.error("根据path查询GitLabProject发生异常: ", e);
            throw new RuntimeException("根据path查询GitLab项目信息发生异常: " + e.getMessage());
        }
        return project;
    }

    /**
     * 查询项目详细信息，包括统计信息
     *
     * @param idOrPath ID或者路径
     * @return Gitlab项目信息
     */
    public Project getProjectDetail(Object idOrPath) {
        Project project = null;
        try {
            project = gitLabApi.getProjectApi().getProject(idOrPath, Boolean.TRUE);
        } catch (Exception e) {
            log.error("根据path查询GitLabProject发生异常: ", e);
            throw new RuntimeException("根据path查询GitLab项目信息发生异常: " + e.getMessage());
        }
        return project;
    }

    public Project getProjectWithException(Object idOrPath) {
        try {
            Project project = gitLabApi.getProjectApi().getProject(idOrPath);
            return project;
        } catch (GitLabApiException e) {
            if (e.getHttpStatus() == HttpStatus.SC_NOT_FOUND) {
                return null;
            }
            log.error("查询Git项目出现错误， gitProjectId={}", idOrPath, e);
            throw new RuntimeException(String.format("查询Git项目出现错误， gitProjectId=%d", idOrPath));
        }
    }

    /**
     * 查询项目
     *
     * @param idOrPath ID或者路径
     * @param userName 用户名
     * @return Gitlab项目信息
     */
    public Project getProject(Object idOrPath, String userName) {
        Project project = null;
        try {
            project = getGitLabApi(userName).getProjectApi().getProject(idOrPath);
        } catch (Exception e) {
            log.error("根据path查询GitLabProject发生异常: ", e);
            throw new RuntimeException("根据path查询GitLab项目信息发生异常: " + e.getMessage());
        }
        return project;
    }

    /**
     * 查询项目
     * 注意：请注意传入是有权限的用户名
     *
     * @param namespace   命令空间
     * @param projectName 项目名称
     * @param userName    用户名（具有权限的用户）
     * @return Gitlab项目
     */
    public Project getProject(String namespace, String projectName, String userName) {
        Project project;
        try {
            project = getGitLabApi(userName).getProjectApi().getProject(namespace, projectName);
        } catch (Exception e) {
            String message = String.format("根据namespace=%s, projectName=%s 查询GitLabProject发生异常", namespace, projectName);
            log.error(message, e);
            throw new RuntimeException(message + " : " + e.getMessage());
        }
        return project;
    }

    /**
     * 创建Tag
     *
     * @param idOrPath    ID或者路径
     * @param tagName     Tag名称
     * @param ref         源分支名称
     * @param message     信息
     * @param releaseNote 备注
     * @param userName    用户名
     * @return Tag信息
     * @throws GitLabApiException
     */
    public Tag createTag(Object idOrPath, String tagName, String ref, String message, String releaseNote, String userName) throws GitLabApiException {
        try {
            return getGitLabApi(userName).getTagsApi().createTag(idOrPath, tagName, ref, message, releaseNote);
        } catch (GitLabApiException e) {
            if (e.getHttpStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
                throw new RuntimeException("Gitlab项目不存在,无法创建Tag");
            }
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 删除Tag
     *
     * @param idOrPath ID或者路径
     * @param tagName  Tag名称
     * @param userName 用户名
     * @throws GitLabApiException
     */
    public void deleteTag(Object idOrPath, String tagName, String userName) throws GitLabApiException {
        try {
            getGitLabApi(userName).getTagsApi().deleteTag(idOrPath, tagName);
        } catch (GitLabApiException e) {
            if (e.getHttpStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
                throw new RuntimeException("Gitlab项目不存在,无法删除Tag");
            }
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 查询分支集合
     *
     * @param idOrPath ID或者路径
     * @param userName 用户名
     * @return 分支集合
     * @throws GitLabApiException
     */
    public List<Branch> getBranches(Object idOrPath, String userName) {
        try {
            return getGitLabApi(userName).getRepositoryApi().getBranches(idOrPath);
        } catch (GitLabApiException e) {
            if (e.getHttpStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
                throw new RuntimeException("Gitlab项目不存在,无法查询分支");
            } else if (e.getHttpStatus() == Response.Status.FORBIDDEN.getStatusCode()) {
                throw new RuntimeException(String.format("[%s]没有权限查询分支集合", userName));
            }
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 创建分支
     *
     * @param idOrPath   ID或者路径
     * @param branchName 分支名称
     * @param ref        源分支名称
     * @param userName   用户名
     * @return 分支信息
     * @throws GitLabApiException
     */
    public Branch createBranch(Object idOrPath, String branchName, String ref, String userName) throws GitLabApiException {
        try {
            return getGitLabApi(userName).getRepositoryApi().createBranch(idOrPath, branchName, ref);
        } catch (GitLabApiException e) {
            if (e.getHttpStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
                throw new RuntimeException("Gitlab项目不存在,无法创建分支");
            }
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 删除分支
     *
     * @param idOrPath   ID或者路径
     * @param branchName 分支名称
     * @param userName   用户名
     * @throws GitLabApiException
     */
    public void deleteBranch(Object idOrPath, String branchName, String userName) throws GitLabApiException {
        try {
            log.debug("[{}]删除分支: {}", userName, branchName);
            getGitLabApi(userName).getRepositoryApi().deleteBranch(idOrPath, branchName);
        } catch (GitLabApiException e) {
            if (e.getHttpStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
                throw new RuntimeException("Gitlab项目不存在,无法删除分支");
            }
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 查询Tag集合
     *
     * @param idOrPath ID或者路径
     * @param userName 用户名
     * @return Tag集合
     * @throws GitLabApiException
     */
    public List<Tag> getTags(Object idOrPath, String userName) {
        try {
            return getGitLabApi(userName).getTagsApi().getTags(idOrPath);
        } catch (GitLabApiException e) {
            if (e.getHttpStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
                throw new RuntimeException("Gitlab项目不存在,无法查询分支");
            } else if (e.getHttpStatus() == Response.Status.FORBIDDEN.getStatusCode()) {
                throw new RuntimeException(String.format("[%s]没有权限查询标签集合", userName));
            }
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 分页查询分支提交记录集合
     *
     * @param idOrPath   ID或者路径
     * @param branchName 分支名称
     * @param startDate  提交开始日期
     * @param endDate    提交结束日期
     * @param pageIndex  起始页
     * @param pageSize   页大小
     * @param userName   用户名
     * @return 提交记录集合
     * @throws GitLabApiException
     */
    public List<Commit> getCommits(Object idOrPath, String branchName, Date startDate, Date endDate, Integer pageIndex, Integer pageSize, String userName) {
        try {
            return getGitLabApi(userName).getCommitsApi().getCommits(idOrPath, branchName, startDate, endDate, pageIndex, pageSize);
        } catch (GitLabApiException e) {
            if (e.getHttpStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
                throw new RuntimeException("Gitlab项目不存在,无法分页查询分支提交记录");
            } else if (e.getHttpStatus() == Response.Status.FORBIDDEN.getStatusCode()) {
                throw new RuntimeException(String.format("[%s]没有权限查询分支提交记录", userName));
            }
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 分页查询分支提交记录集合
     *
     * @param idOrPath   ID或者路径
     * @param branchName 分支名称
     * @param startDate  提交开始日期
     * @param endDate    提交结束日期
     * @param userName   用户名
     * @return 提交记录集合
     * @throws GitLabApiException
     */
    public List<Commit> getCommitTotal(Object idOrPath, String branchName, Date startDate, Date endDate, String userName) throws GitLabApiException {
        try {
            return getGitLabApi(userName).getCommitsApi().getCommits(idOrPath, branchName, startDate, endDate);
        } catch (GitLabApiException e) {
            if (e.getHttpStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
                throw new RuntimeException("Gitlab项目不存在,无法分页查询分支提交记录总数");
            }
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 查询提交记录
     *
     * @param idOrPath ID或者路径
     * @param sha      提交记录Id(哈希码)
     * @param userName 用户名
     * @return 提交记录信息
     * @throws GitLabApiException
     */
    public Commit getCommit(Object idOrPath, String sha, String userName) throws GitLabApiException {
        try {
            return getGitLabApi(userName).getCommitsApi().getCommit(idOrPath, sha);
        } catch (GitLabApiException e) {
            if (e.getHttpStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
                throw new RuntimeException("Gitlab项目不存在,无法查询提交记录");
            }
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 通过tagName查询Tag对象
     *
     * @param idOrPath ID或者路径
     * @param tagName  tag标签名
     * @return Tag 标签对象
     * @throws GitLabApiException
     */
    public Tag getTagByName(Object idOrPath, String tagName) {
        try {
            return gitLabApi.getTagsApi().getTag(idOrPath, tagName);
        } catch (GitLabApiException e) {
            if (e.getHttpStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
                return null;
            } else if (e.getHttpStatus() == Response.Status.FORBIDDEN.getStatusCode()) {
                throw new RuntimeException("error.git.tag.query.no.permission");
            }
            log.error("getTagByName error：", e);
            throw new RuntimeException("error.git.tag.query.error");
        }
    }
}
