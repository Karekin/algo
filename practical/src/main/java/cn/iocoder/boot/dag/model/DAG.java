package cn.iocoder.boot.dag.model;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 表示一个有向无环图（DAG）。
 * 提供基本的增删、拓扑排序、环检测等能力。
 */
public class DAG {
    private final Set<Node> nodes;
    private final Set<Edge> edges;

    public DAG() {
        this.nodes = new LinkedHashSet<>();
        this.edges = new LinkedHashSet<>();
    }

    public DAG(Collection<Node> nodes, Collection<Edge> edges) {
        this.nodes = new LinkedHashSet<>(nodes);
        this.edges = new LinkedHashSet<>(edges);
    }

    public Set<Node> getNodes() {
        return nodes;
    }

    public Set<Edge> getEdges() {
        return edges;
    }

    public void addNode(Node node) {
        nodes.add(node);
    }

    public void addEdge(Node from, Node to) {
        // 确保两端节点也进 nodes 集合
        nodes.add(from);
        nodes.add(to);
        edges.add(new Edge(from, to));
    }

    public void addEdge(Edge edge) {
        addEdge(edge.getFrom(), edge.getTo());
    }

    /**
     * 检测当前图是否存在环 (Kahn算法)
     */
    public boolean hasCycle() {
        // 1. 计算每个节点的入度
        Map<Node, Integer> inDegree = new HashMap<>();
        for (Node n : nodes) {
            inDegree.put(n, 0);
        }
        for (Edge e : edges) {
            inDegree.put(e.getTo(), inDegree.get(e.getTo()) + 1);
        }

        // 2. 找到所有入度为0的节点
        Queue<Node> queue = new LinkedList<>();
        for (Map.Entry<Node, Integer> entry : inDegree.entrySet()) {
            if (entry.getValue() == 0) {
                queue.offer(entry.getKey());
            }
        }

        int visitedCount = 0;
        while (!queue.isEmpty()) {
            Node current = queue.poll();
            visitedCount++;
            // 将 current 的所有后继节点入度 -1
            for (Edge e : edges) {
                if (e.getFrom().equals(current)) {
                    Node succ = e.getTo();
                    int deg = inDegree.get(succ) - 1;
                    inDegree.put(succ, deg);
                    if (deg == 0) {
                        queue.offer(succ);
                    }
                }
            }
        }
        // 若最终被移除的节点数 < 总数，说明有环
        return visitedCount < nodes.size();
    }

    /**
     * 返回图的拓扑排序，若有环则返回空列表
     */
    public List<Node> topologicalSort() {
        if (hasCycle()) {
            return Collections.emptyList();
        }
        Map<Node, Integer> inDegree = new HashMap<>();
        for (Node n : nodes) {
            inDegree.put(n, 0);
        }
        for (Edge e : edges) {
            inDegree.put(e.getTo(), inDegree.get(e.getTo()) + 1);
        }

        Queue<Node> queue = new LinkedList<>();
        for (Map.Entry<Node, Integer> entry : inDegree.entrySet()) {
            if (entry.getValue() == 0) {
                queue.offer(entry.getKey());
            }
        }

        List<Node> result = new ArrayList<>();
        while (!queue.isEmpty()) {
            Node current = queue.poll();
            result.add(current);
            for (Edge e : edges) {
                if (e.getFrom().equals(current)) {
                    Node succ = e.getTo();
                    inDegree.put(succ, inDegree.get(succ) - 1);
                    if (inDegree.get(succ) == 0) {
                        queue.offer(succ);
                    }
                }
            }
        }
        return result;
    }

    @Override
    public String toString() {
        String ns = nodes.stream().map(Node::toString).collect(Collectors.joining(", "));
        String es = edges.stream().map(Edge::toString).collect(Collectors.joining(", "));
        return "DAG{\n  nodes=[" + ns + "],\n  edges=[" + es + "]\n}";
    }
}

