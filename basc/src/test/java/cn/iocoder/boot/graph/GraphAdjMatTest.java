package cn.iocoder.boot.graph;

import cn.graph.GraphAdjMat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GraphAdjMatTest {
    private GraphAdjMat graph;

    @BeforeEach
    public void setUp() {
        // 初始化顶点和边的数组
        int[] vertices = {1, 2, 3, 4};
        int[][] edges = {
                {0, 1},
                {1, 2},
                {2, 3}
        };
        // 创建图实例
        graph = new GraphAdjMat(vertices, edges);
    }

    // 测试图的初始化和邻接矩阵的正确性
    @Test
    public void testGraphInitialization() {
        assertEquals(4, graph.size(), "图的顶点数量应为 4");

        // 检查邻接矩阵是否正确
        String expectedMatrix = "[[0, 1, 0, 0], [1, 0, 1, 0], [0, 1, 0, 1], [0, 0, 1, 0]]";
        assertEquals(expectedMatrix, graph.getAdjMat().toString(), "邻接矩阵的初始状态应与预期相符");
    }

    // 测试添加顶点
    @Test
    public void testAddVertex() {
        graph.addVertex(5);
        assertEquals(5, graph.size(), "添加顶点后，图的顶点数量应为 5");

        // 验证邻接矩阵的大小是否正确
        assertEquals(5, graph.getAdjMat().size(), "邻接矩阵应增加到 5 行");
        assertEquals(5, graph.getAdjMat().get(0).size(), "邻接矩阵应增加到 5 列");

        // 新增的顶点与原顶点无连接
        assertEquals("[0, 0, 0, 0, 0]", graph.getAdjMat().get(4).toString(), "新顶点行应初始化为全 0");
    }

    // 测试删除顶点
    @Test
    public void testRemoveVertex() {
        graph.removeVertex(1); // 删除顶点 2
        assertEquals(3, graph.size(), "删除顶点后，图的顶点数量应为 3");

        // 检查邻接矩阵是否更新正确
        String expectedMatrixAfterDeletion = "[[0, 0, 0], [0, 0, 1], [0, 1, 0]]";
        assertEquals(expectedMatrixAfterDeletion, graph.getAdjMat().toString(), "删除顶点后的邻接矩阵应符合预期");
    }

    // 测试添加边
    @Test
    public void testAddEdge() {
        graph.addEdge(0, 3); // 添加顶点 1 和顶点 4 之间的边
        assertEquals(1, graph.getAdjMat().get(0).get(3), "顶点 1 和顶点 4 之间应有一条边");
        assertEquals(1, graph.getAdjMat().get(3).get(0), "无向图中顶点 4 和顶点 1 之间应对称有边");
    }

    // 测试删除边
    @Test
    public void testRemoveEdge() {
        graph.removeEdge(1, 2); // 删除顶点 2 和顶点 3 之间的边
        assertEquals(0, graph.getAdjMat().get(1).get(2), "删除边后，顶点 2 和顶点 3 之间不应有边");
        assertEquals(0, graph.getAdjMat().get(2).get(1), "无向图中，删除边后顶点 3 和顶点 2 之间也不应有边");
    }

    // 测试邻接矩阵输出格式
    @Test
    public void testPrint() {
        System.out.println("初始图的邻接矩阵：");
        graph.print();

        // 检查输出的邻接矩阵是否包含所有顶点行
        String expectedVertices = "顶点列表 = [1, 2, 3, 4]";
        String expectedMatrix = "[[0, 1, 0, 0], [1, 0, 1, 0], [0, 1, 0, 1], [0, 0, 1, 0]]";

        assertEquals(expectedVertices, "顶点列表 = " + graph.getVertices().toString(), "顶点列表应符合初始化值");
        assertEquals(expectedMatrix, graph.getAdjMat().toString(), "邻接矩阵应符合初始化值");
    }
}

