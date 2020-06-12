package com.xiaxinyu.gitlab.client.api.model;

import lombok.Builder;
import lombok.Data;

/**
 * 合并请求
 *
 * @author XIAXINYU3
 * @date 2019.8.27
 */
@Data
@Builder
public class MergeRequestDiffDTO {
    private String oldPath;
    private String newPath;
    private String aMode;
    private String bMode;
    private String diff;
    private Boolean newFile;
    private Boolean renamedFile;
    private Boolean deletedFile;
}
