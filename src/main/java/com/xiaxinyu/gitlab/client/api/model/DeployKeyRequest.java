package com.xiaxinyu.gitlab.client.api.model;

/**
 * @author XIAXINYU3
 * DeployKey 实体类
 */
public class DeployKeyRequest {
    private Object projectIdOrPath;
    private Integer keyId;
    private String title;
    private String key;
    private Boolean canPush;

    public DeployKeyRequest(){}

    public Object getProjectIdOrPath() {
        return projectIdOrPath;
    }

    public void setProjectIdOrPath(Object projectIdOrPath) {
        this.projectIdOrPath = projectIdOrPath;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Boolean getCanPush() {
        return canPush;
    }

    public void setCanPush(Boolean canPush) {
        this.canPush = canPush;
    }

    public Integer getKeyId() {
        return keyId;
    }

    public void setKeyId(Integer keyId) {
        this.keyId = keyId;
    }

    @Override
    public String toString() {
        return "DeployKey{" +
                "projectIdOrPath=" + projectIdOrPath +
                ", keyId='" + (keyId != null ? keyId : 0) + '\'' +
                ", title='" + title + '\'' +
                ", key='" + key + '\'' +
                ", canPush=" + canPush +
                '}';
    }
}
