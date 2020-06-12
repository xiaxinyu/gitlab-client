package com.xiaxinyu.gitlab.client.api.model;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 合并请求
 *
 * @author XIAXINYU3
 * @date 2019.8.27
 */
@Data
public class MergeRequestDTO {
    private Integer iid;
    private String title;
    private String targetBranch;
    private String sourceBranch;
    private Integer authorId;
    private String author;
    private Integer assigneeId;
    private String assignee;
    private String description;
    private String state;
    private Date createdAt;
    private Date updateAt;
    private String mergeStatus;
    private Boolean atMeFlag = Boolean.FALSE;
    private Boolean canMerge = Boolean.FALSE;
    private Boolean canClose = Boolean.FALSE;
    private List<MergeRequestDiffDTO> diffs;
    private List<MergeRequestCommitDTO> commits;
}
