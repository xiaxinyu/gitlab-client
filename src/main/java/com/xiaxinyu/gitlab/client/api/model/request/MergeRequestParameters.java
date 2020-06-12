package com.xiaxinyu.gitlab.client.api.model.request;

import lombok.Data;
import lombok.ToString;

/**
 * 合并请求
 *
 * @author XIAXINYU3
 * @date 2019.8.27
 */
@Data
@ToString
public class MergeRequestParameters {
    private String title;
    private String targetBranch;
    private String sourceBranch;
    private Integer assigneeId;
    private String assignee;
    private String description;
}
