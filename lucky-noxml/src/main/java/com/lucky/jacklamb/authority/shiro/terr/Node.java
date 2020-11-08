package com.lucky.jacklamb.authority.shiro.terr;

import com.lucky.jacklamb.authority.shiro.entity.NodeModel;

import java.util.ArrayList;
import java.util.List;

/**
 * 数型结构封装
 * @author fk7075
 * @version 1.0.0
 * @date 2020/11/8 8:53 下午
 */
public class Node<Data extends NodeModel<ID>,ID> {

    private Data data;
    private List<Node<Data,ID>> childTree=new ArrayList<>();

    public Node(Data data) {
        this.data = data;
    }

    public ID getId() {
        return data.getId();
    }

    public ID getParentId() {
        return data.getParentId();
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public List<Node<Data,ID>> getChildTree() {
        return childTree;
    }

    public void setChildTree(List<Node<Data,ID>> childTree) {
        this.childTree = childTree;
    }
}
