package com.xiaxinyu.gitlab.client.api.model;

/**
 * @program: devopsci
 * @description: 编译分支
 * @author: dushaohua5
 * @create: 2019-04-15 12:47
 */
public class GitBranch {
    private String name;
    private String path;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
