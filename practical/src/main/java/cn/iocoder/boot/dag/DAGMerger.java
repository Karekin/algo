package cn.iocoder.boot.dag;

import cn.iocoder.boot.dag.model.DAG;
import cn.iocoder.boot.dag.model.Edge;
import cn.iocoder.boot.dag.model.Node;

import java.util.*;

/**
 * 演示如何合并两个 DAG 时，
 *  - 识别相似子结构 (二节点线性+部门相同) 并合并节点
 *  - 余下节点拼接到合并后图中
 */
public class DAGMerger {

    /**
     * 将 g2 合并到 g1，返回一个新的 DAG。
     * 若检测到环，则抛异常。
     */
    public DAG merge(DAG g1, DAG g2) {
        // 拷贝 g1 构造起始合并图
        DAG merged = new DAG(g1.getNodes(), g1.getEdges());

        // 1. 找到 g1, g2 中“二节点直线子图”的所有组合，并进行匹配
        Map<Node, Node> nodeMapping = findSimilarSubgraphs(merged, g2);

        // 2. 把 g2 中尚未匹配的节点追加到 merged
        Map<Node, Node> newCreated = new HashMap<>();
        for (Node n2 : g2.getNodes()) {
            if (nodeMapping.containsKey(n2)) {
                // 已匹配到 g1 节点
                continue;
            }
            // 否则创建一个新节点
            Node fresh = new Node(n2.getId(), n2.getDepartment());
            merged.addNode(fresh);
            newCreated.put(n2, fresh);
        }

        // 3. 把 g2 的边也合并进 merged
        //    对于某条边 (from2->to2)，若 from2/to2 被映射，则使用对应节点；否则使用 newlyCreated 的节点
        for (Edge e2 : g2.getEdges()) {
            Node from2 = e2.getFrom();
            Node to2   = e2.getTo();

            // 根据映射或 newCreated 找到合并后节点
            Node mappedFrom = nodeMapping.getOrDefault(from2, newCreated.getOrDefault(from2, from2));
            Node mappedTo   = nodeMapping.getOrDefault(to2,   newCreated.getOrDefault(to2,   to2));

            merged.addEdge(mappedFrom, mappedTo);
        }

        // 4. 检查环
        if (merged.hasCycle()) {
            throw new RuntimeException("Merge produced a cycle! Not a DAG anymore.");
        }

        return merged;
    }

    /**
     * 在 g2 中找到与 g1 中二节点直线子结构相似的部分。
     * 返回一个映射: g2节点 -> g1节点 (表示这两个节点可视为同一个)
     *
     * 注意：此处仅示例“二节点直线 (X->Y) + 部门相同”即认为相似。
     * 如需更复杂的子图同构，可自行扩展。
     */
    private Map<Node, Node> findSimilarSubgraphs(DAG g1, DAG g2) {
        Map<Node, Node> result = new HashMap<>();

        // 1. 收集 g1 与 g2 中所有“二节点直线”
        List<TwoChain> chains1 = findAllTwoChains(g1);
        List<TwoChain> chains2 = findAllTwoChains(g2);

        // 2. 两两对比，若部门相同则认定可合并
        //    演示：B->C(deptB->deptC) vs Y->W(deptB->deptC)
        for (TwoChain c1 : chains1) {
            for (TwoChain c2 : chains2) {
                if (isSimilar(c1, c2)) {
                    // 将 c2 的节点映射到 c1 的节点
                    result.put(c2.n1, c1.n1);
                    result.put(c2.n2, c1.n2);
                }
            }
        }
        return result;
    }

    /**
     * 找到给定DAG中所有形如 (n1->n2) 的二节点链
     */
    private List<TwoChain> findAllTwoChains(DAG dag) {
        List<TwoChain> list = new ArrayList<>();
        for (Edge e : dag.getEdges()) {
            list.add(new TwoChain(e.getFrom(), e.getTo()));
        }
        return list;
    }

    /**
     * 判断两个二节点链 (A->B) 与 (X->Y) 是否“相似”，
     * 这里演示：只要 department 相同即可。
     */
    private boolean isSimilar(TwoChain c1, TwoChain c2) {
        return c1.n1.getDepartment().equals(c2.n1.getDepartment())
                && c1.n2.getDepartment().equals(c2.n2.getDepartment());
    }

    /**
     * 用于描述 "二节点直线" 的辅助结构
     */
    private static class TwoChain {
        Node n1;
        Node n2;
        public TwoChain(Node n1, Node n2) {
            this.n1 = n1;
            this.n2 = n2;
        }
    }

    public static void main(String[] args) {
        // ========== 1. 构建 DAG1 ==========
        // A->B->C： A(deptA), B(deptB), C(deptC)
        DAG g1 = new DAG();
        Node A = new Node("A", "deptA");
        Node B = new Node("B", "deptB");
        Node C = new Node("C", "deptC");
        g1.addEdge(A, B);
        g1.addEdge(B, C);

        // ========== 2. 构建 DAG2 ==========
        // 并行：X->Y, X->Z, Y->W, Z->W
        // X(deptX), Y(deptB), W(deptC), Z(deptZ)
        DAG g2 = new DAG();
        Node X = new Node("X", "deptX");
        Node Y = new Node("Y", "deptB");  // <-- deptB, 将匹配到 B
        Node W = new Node("W", "deptC");  // <-- deptC, 将匹配到 C
        Node Z = new Node("Z", "deptZ");  // 不与 g1 中节点匹配
        g2.addEdge(X, Y);
        g2.addEdge(X, Z);
        g2.addEdge(Y, W);
        g2.addEdge(Z, W);

        // ========== 3. 合并 ==========
        DAGMerger merger = new DAGMerger();
        DAG merged = merger.merge(g1, g2);

        // ========== 4. 打印结果 ==========
        System.out.println("DAG1 => \n" + g1);
        System.out.println("DAG2 => \n" + g2);
        System.out.println("Merged => \n" + merged);

        // 拓扑排序 & 检查环
        System.out.println("Merged has cycle? " + merged.hasCycle());
        System.out.println("Merged topological sort => " + merged.topologicalSort());
    }
}


