package com.xiaxinyu.gitlab.client.api.model;

import lombok.*;

/**
 * @author XIAXINYU3
 * DeployKey 实体类
 */
@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeployKeyRequest {
    private Object projectIdOrPath;
    private Integer keyId;
    private String title;
    private String key;
    private Boolean canPush;
}
