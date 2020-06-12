package com.xiaxinyu.gitlab.client.api.model;

import java.util.List;

/**
 * @program: devopsci
 * @description: Git的分支、tag等
 * @author: dushaohua5
 * @create: 2019-04-15 12:47
 */
public class GitRefs {
    //类型，tags或者branches
    private String label;
    //各类型的列表
    private List list;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List getList() {
        return list;
    }

    public void setList(List list) {
        this.list = list;
    }
}
