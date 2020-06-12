package com.xiaxinyu.gitlab.client.api.model;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

/**
 * 合并请求提交记录
 *
 * @author XIAXINYU3
 * @date 2019.8.29
 */
@Data
@Builder
public class MergeRequestCommitDTO {
    private String title;
    private String authorName;
    private Date createAt;
    private Date commitAt;
    private String message;
}
