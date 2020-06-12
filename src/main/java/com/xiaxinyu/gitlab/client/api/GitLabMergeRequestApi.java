package com.xiaxinyu.gitlab.client.api;


import com.xiaxinyu.gitlab.client.api.model.MergeRequestParams;
import com.xiaxinyu.gitlab.client.api.model.request.MergeRequestParameters;
import lombok.extern.slf4j.Slf4j;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.MergeRequestApi;
import org.gitlab4j.api.models.Commit;
import org.gitlab4j.api.models.CompareResults;
import org.gitlab4j.api.models.MergeRequest;
import org.gitlab4j.api.models.MergeRequestFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Objects;

import org.gitlab4j.api.Constants.StateEvent;

/**
 * GitLab 合并请求API
 *
 * @author XIAXINYU3
 * @date 2019.8.27
 */
@Slf4j
@Component
public class GitLabMergeRequestApi {

    @Autowired
    GitLabApi gitLabApi;

    @Autowired
    GitLabApiExtend gitLabApiExtend;

    private GitLabApi getGitLabApiAPI(String curUser) throws GitLabApiException {
        gitLabApi.sudo(curUser);
        return gitLabApi;
    }

    public List<MergeRequest> getMergeRequestsByState(MergeRequestParams params) {
        try {
            MergeRequestApi mrApi = gitLabApi.getMergeRequestApi();
            MergeRequestFilter filter = new MergeRequestFilter();
            filter.setState(params.getState());
            filter.setProjectId(params.getGitProjectId());
            if (Objects.nonNull(params.getAuthorId())) {
                filter.setAuthorId(params.getAuthorId());
            }
            if (Objects.nonNull(params.getAssigneeId())) {
                filter.setAssigneeId(params.getAssigneeId());
            }
            List<MergeRequest> mrs;
            if (Objects.isNull(params.getLength()) && Objects.isNull(params.getPage())) {
                log.debug("分页参数为空");
                mrs = mrApi.getMergeRequests(filter);
            } else {
                log.debug("分页参数不为空");
                mrs = mrApi.getMergeRequests(filter, params.getPage(), params.getLength());
            }
            return mrs;
        } catch (GitLabApiException e) {
            throw new RuntimeException(String.format("根据状态获取合并请求出现错误 : %s", e.getMessage()));
        }
    }

    public MergeRequest findMergeRequest(Integer gitProjectId, Integer mergeRequestId) {
        try {
            MergeRequestApi mrApi = gitLabApi.getMergeRequestApi();
            return mrApi.getMergeRequest(gitProjectId, mergeRequestId);
        } catch (GitLabApiException e) {
            throw new RuntimeException(String.format("获取合并请求出现错误 : %s", e.getMessage()));
        }
    }

    public MergeRequest findMergeRequestWithChanges(Integer gitProjectId, Integer mergeRequestId) {
        try {
            MergeRequestApi mrApi = gitLabApi.getMergeRequestApi();
            return mrApi.getMergeRequestChanges(gitProjectId, mergeRequestId);
        } catch (GitLabApiException e) {
            throw new RuntimeException(String.format("获取合并请求出现错误 : %s", e.getMessage()));
        }
    }

    public List<Commit> findCommits(Integer gitProjectId, Integer mergeRequestId) {
        try {
            MergeRequestApi mrApi = gitLabApi.getMergeRequestApi();
            return mrApi.getCommits(gitProjectId, mergeRequestId);
        } catch (GitLabApiException e) {
            throw new RuntimeException(String.format("获取合并请求的提交记录出现错误 : %s", e.getMessage()));
        }
    }

    public MergeRequest createMergeRequest(String author, Integer gitProjectId, MergeRequestParameters params) {
        try {
            MergeRequestApi mrApi = getGitLabApiAPI(author).getMergeRequestApi();
            return mrApi.createMergeRequest(gitProjectId, params.getSourceBranch(), params.getTargetBranch(), params.getTitle(), params.getDescription(), params.getAssigneeId());
        } catch (GitLabApiException e) {
            throw new RuntimeException(String.format("创建合并请求出现错误 : %s", e.getMessage()));
        }
    }

    public void acceptMergeRequest(String author, Integer gitProjectId, Integer mergeRequestId, Boolean shouldRemoveSourceBranch) {
        try {
            log.info("执行合并请求当前用户：{}", author);
            MergeRequestApi mrApi = getGitLabApiAPI(author).getMergeRequestApi();
            mrApi.acceptMergeRequest(gitProjectId, mergeRequestId, "merged", false, false);
        } catch (GitLabApiException e) {
            if (e.getHttpStatus() == Response.Status.METHOD_NOT_ALLOWED.getStatusCode()) {
                log.warn("存在冲突，合并请求不被允许, statusCode={}", e.getHttpStatus());
                throw new RuntimeException("存在冲突，合并请求不被允许");
            }
            if (e.getHttpStatus() == Response.Status.NOT_ACCEPTABLE.getStatusCode()) {
                log.warn("合并请求被关闭或者合并，合并请求不再被允许, statusCode={}", e.getHttpStatus());
                throw new RuntimeException("合并请求被关闭或者合并，合并请求不再被允许");
            }
            throw new RuntimeException(String.format("合并请求出现错误 : %s", e.getMessage()));
        }
    }

    public MergeRequest operateMergeRequest(String author, Integer gitProjectId, Integer mergeRequestId, StateEvent event) {
        try {
            MergeRequestApi mrApi = getGitLabApiAPI(author).getMergeRequestApi();
            return mrApi.updateMergeRequest(gitProjectId, mergeRequestId, null, null, null, null, event, null, null, null, null, null, null);
        } catch (GitLabApiException e) {
            throw new RuntimeException(String.format("操作合并请求事件状态出现错误 : %s", e.getMessage()));
        }
    }

    public CompareResults compareBranches(String author, Integer gitProjectId, String from, String to) {
        try {
            log.info("合并请求当前用户：{}", author);
            //因为业务上的from和to， from是超前于to， 所以交换from到to
            CompareResults compareResults = getGitLabApiAPI(author).getRepositoryApi().compare(gitProjectId, to, from, true);
            return compareResults;
        } catch (GitLabApiException e) {
            throw new RuntimeException(String.format("比较分支出现错误 : %s", e.getMessage()));
        }
    }

    public String getChanges() {
        String data = gitLabApiExtend.getPlainText(null, "http://gitlab.devopsuat.crc.com.cn/threetest-threetest/zeus-test01/merge_requests/5/commits.json");
        return data;
    }
}
