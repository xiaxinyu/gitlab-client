package com.xiaxinyu.gitlab.client.api.model;

import lombok.Data;
import lombok.ToString;
import org.gitlab4j.api.Constants.MergeRequestState;

/**
 * 合并请求
 *
 * @author XIAXINYU3
 * @date 2019.8.27
 */
@Data
@ToString
public class MergeRequestParams {
    private Integer gitProjectId;
    private MergeRequestState state;
    private Integer authorId;
    private Integer assigneeId;
    private Integer page;
    private Integer length;
}
