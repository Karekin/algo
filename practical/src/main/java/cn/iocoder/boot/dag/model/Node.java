package cn.iocoder.boot.dag.model;

import java.util.Objects;

/**
 * 表示图中的一个节点。
 * 可包含业务上的若干字段（如部门信息）来辅助判断“是否可共用”。
 */
public class Node {
    private final String id;         // 节点唯一ID
    private final String department; // 节点对应的部门，用于演示合并条件

    public Node(String id, String department) {
        this.id = id;
        this.department = department;
    }

    public String getId() {
        return id;
    }

    public String getDepartment() {
        return department;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Node)) return false;
        Node node = (Node) o;
        // 简化策略：仅根据 id 判断是否相等
        return Objects.equals(id, node.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Node(" + id + ", dept=" + department + ")";
    }
}

