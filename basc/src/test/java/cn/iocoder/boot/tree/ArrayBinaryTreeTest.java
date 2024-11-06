package cn.iocoder.boot.tree;

import cn.tree.ArrayBinaryTree;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ArrayBinaryTreeTest {
    private ArrayBinaryTree tree;

    // 初始化测试用的二叉树，数组表示的树结构为：1, 2, 3, 4, 5, 6, 7
    @BeforeEach
    public void setUp() {
        tree = new ArrayBinaryTree(Arrays.asList(1, 2, 3, 4, 5, 6, 7));
    }

    // 测试树的大小
    @Test
    public void testSize() {
        assertEquals(7, tree.size(), "树的大小应为 7");
    }

    // 测试获取指定索引的节点值
    @Test
    public void testVal() {
        assertEquals(1, tree.val(0), "根节点值应为 1");
        assertEquals(2, tree.val(1), "节点 1 的值应为 2");
        assertEquals(3, tree.val(2), "节点 2 的值应为 3");
        assertNull(tree.val(10), "索引 10 超出范围，应返回 null");
    }

    // 测试获取左子节点的索引
    @Test
    public void testLeft() {
        assertEquals(1, tree.left(0), "节点 0 的左子节点索引应为 1");
        assertEquals(3, tree.left(1), "节点 1 的左子节点索引应为 3");
        assertEquals(5, tree.left(2), "节点 2 的左子节点索引应为 5");
    }

    // 测试获取右子节点的索引
    @Test
    public void testRight() {
        assertEquals(2, tree.right(0), "节点 0 的右子节点索引应为 2");
        assertEquals(4, tree.right(1), "节点 1 的右子节点索引应为 4");
        assertEquals(6, tree.right(2), "节点 2 的右子节点索引应为 6");
    }

    // 测试获取父节点的索引
    @Test
    public void testParent() {
        assertEquals(0, tree.parent(1), "节点 1 的父节点索引应为 0");
        assertEquals(0, tree.parent(2), "节点 2 的父节点索引应为 0");
        assertEquals(1, tree.parent(3), "节点 3 的父节点索引应为 1");
        assertEquals(1, tree.parent(4), "节点 4 的父节点索引应为 1");
    }

    // 测试层序遍历
    @Test
    public void testLevelOrder() {
        List<Integer> expectedOrder = Arrays.asList(1, 2, 3, 4, 5, 6, 7);
        assertEquals(expectedOrder, tree.levelOrder(), "层序遍历应为 [1, 2, 3, 4, 5, 6, 7]");
    }

    // 测试前序遍历
    @Test
    public void testPreOrder() {
        List<Integer> expectedOrder = Arrays.asList(1, 2, 4, 5, 3, 6, 7);
        assertEquals(expectedOrder, tree.preOrder(), "前序遍历应为 [1, 2, 4, 5, 3, 6, 7]");
    }

    // 测试中序遍历
    @Test
    public void testInOrder() {
        List<Integer> expectedOrder = Arrays.asList(4, 2, 5, 1, 6, 3, 7);
        assertEquals(expectedOrder, tree.inOrder(), "中序遍历应为 [4, 2, 5, 1, 6, 3, 7]");
    }

    // 测试后序遍历
    @Test
    public void testPostOrder() {
        List<Integer> expectedOrder = Arrays.asList(4, 5, 2, 6, 7, 3, 1);
        assertEquals(expectedOrder, tree.postOrder(), "后序遍历应为 [4, 5, 2, 6, 7, 3, 1]");
    }
}

