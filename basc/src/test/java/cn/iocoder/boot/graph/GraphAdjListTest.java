package cn.iocoder.boot.graph;

import cn.graph.GraphAdjList;
import cn.graph.GraphAdjList.Vertex;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class GraphAdjListTest {
    private GraphAdjList graph;
    private Vertex v1, v2, v3, v4, v5;

    // 初始化测试图
    @BeforeEach
    public void setUp() {
        // 创建顶点
        v1 = new Vertex(1);
        v2 = new Vertex(2);
        v3 = new Vertex(3);
        v4 = new Vertex(4);
        v5 = new Vertex(5);

        // 创建图并添加一些边
        Vertex[][] edges = {
                {v1, v2},
                {v2, v3},
                {v3, v4},
                {v4, v5},
                {v1, v5}
        };
        graph = new GraphAdjList(edges);
    }

    // 测试图的初始化
    @Test
    public void testInitialization() {
        assertEquals(5, graph.size(), "初始化后图应包含 5 个顶点");

        // 检查邻接表结构
        assertTrue(graph.getAdjList().get(v1).contains(v2), "v1 应与 v2 相邻");
        assertTrue(graph.getAdjList().get(v2).contains(v3), "v2 应与 v3 相邻");
        assertTrue(graph.getAdjList().get(v3).contains(v4), "v3 应与 v4 相邻");
        assertTrue(graph.getAdjList().get(v4).contains(v5), "v4 应与 v5 相邻");
    }

    // 测试添加顶点
    @Test
    public void testAddVertex() {
        graph.addVertex(new Vertex(6));
        assertEquals(6, graph.size(), "添加顶点后，图的顶点数量应为 6");
    }

    // 测试删除顶点
    @Test
    public void testRemoveVertex() {
        graph.removeVertex(v2);
        assertEquals(4, graph.size(), "删除顶点后，图的顶点数量应为 4");
    }

    // 测试广度优先遍历
    @Test
    public void testGraphBFS() {
        List<Vertex> bfsResult = graph.graphBFS(v1);
        List<Integer> expectedBFS = Arrays.asList(1, 2, 5, 3, 4);
        assertEquals(expectedBFS, getValues(bfsResult), "BFS 遍历序列应为 [1, 2, 5, 3, 4]");
    }

    // 测试深度优先遍历
    @Test
    public void testGraphDFS() {
        List<Vertex> dfsResult = graph.graphDFS(v1);
        List<Integer> expectedDFS = Arrays.asList(1, 2, 3, 4, 5);
        assertEquals(expectedDFS, getValues(dfsResult), "DFS 遍历序列应为 [1, 2, 3, 4, 5]");
    }

    // 辅助方法：获取顶点值的列表，用于验证遍历结果
    private List<Integer> getValues(List<Vertex> vertices) {
        List<Integer> values = new ArrayList<>();
        for (Vertex vertex : vertices) {
            values.add(vertex.val);
        }
        return values;
    }

}
