package com.xiaxinyu.gitlab.client.api.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 合并请求
 *
 * @author XIAXINYU3
 * @date 2019.8.27
 */
@Data
@Builder
public class MergeRequestCompareDTO {
    private List<MergeRequestDiffDTO> diffs;
    private List<MergeRequestCommitDTO> commits;
}
