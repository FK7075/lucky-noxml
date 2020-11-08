package com.lucky.jacklamb.authority.shiro.terr;

import com.lucky.jacklamb.authority.shiro.entity.NodeModel;

import java.util.ArrayList;
import java.util.List;

/**
 * @author fk7075
 * @version 1.0.0
 * @date 2020/11/8 9:00 下午
 */
public class NodeUtils<Data extends NodeModel<ID>,ID> {

    private List<Node<Data,ID>> simpleNodes;

    public NodeUtils(List<Data> dates){
        simpleNodes=new ArrayList<>();
        dates.stream().forEach(data -> simpleNodes.add(new Node<>(data)));
    }

    /**
     * 通过ID获取一个简单的节点
     * @param id 节点ID
     * @return 节点
     */
    private Node<Data,ID> getSimpleNodeById(ID id){
        for (Node<Data,ID> node : simpleNodes) {
            if(node.getId().equals(id)){
                return node;
            }
        }
        return null;
    }

    /**
     * 通过ID获取以该节点对应的所有简单子节点
     * @param id 节点ID
     * @return 简单子数
     */
    private List<Node<Data,ID>> getSimpleChildTreeById(ID id){
        List<Node<Data,ID>> simpleSubTree=new ArrayList<>();
        for (Node<Data,ID> node : simpleNodes) {
            if(node.getParentId().equals(id)){
                simpleSubTree.add(node);
            }
        }
        return simpleSubTree;
    }

    /**
     * 根据ID获得一颗完整的树
     * @param id 节点ID
     * @return 完整的数
     */
    public Node<Data,ID> getNodeById(ID id){
        Node<Data,ID> root=getSimpleNodeById(id);
        List<Node<Data,ID>> childTree = getSimpleChildTreeById(id);
        for (Node<Data,ID> node : childTree) {
            Node<Data,ID> completeNode = getNodeById(node.getId());
            root.getChildTree().add(completeNode);
        }
        return root;
    }
}
