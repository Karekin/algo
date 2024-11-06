package cn.iocoder.boot.tree;

import cn.tree.BinarySearchTree;
import cn.tree.BinarySearchTree.TreeNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BinarySearchTreeTest {
    private BinarySearchTree bst;

    // 初始化测试用的二叉搜索树
    @BeforeEach
    public void setUp() {
        bst = new BinarySearchTree();
        bst.insert(5);
        bst.insert(3);
        bst.insert(8);
        bst.insert(2);
        bst.insert(4);
        bst.insert(7);
        bst.insert(9);
    }

    // 测试插入节点
    @Test
    public void testInsert() {
        bst.insert(6);
        assertEquals(6, bst.search(6).val, "插入值 6 后应能在树中找到该值");
    }

    // 测试查找节点
    @Test
    public void testSearch() {
        TreeNode result = bst.search(3);
        assertNotNull(result, "查找值 3 应存在于树中");
        assertEquals(3, result.val, "查找到的节点值应为 3");

        assertNull(bst.search(10), "查找值 10 应不存在于树中");
    }

    // 测试删除节点
    @Test
    public void testRemove() {
        bst.remove(3); // 删除值为 3 的节点
        assertNull(bst.search(3), "删除值 3 后应无法在树中找到该值");

        bst.remove(5); // 删除根节点
        assertNull(bst.search(5), "删除根节点值 5 后应无法在树中找到该值");
    }

    // 测试前序遍历
    @Test
    public void testPreOrder() {
        List<Integer> expectedOrder = Arrays.asList(5, 3, 2, 4, 8, 7, 9);
        assertEquals(expectedOrder, bst.preOrder(), "前序遍历应为 [5, 3, 2, 4, 8, 7, 9]");
    }

    // 测试中序遍历
    @Test
    public void testInOrder() {
        List<Integer> expectedOrder = Arrays.asList(2, 3, 4, 5, 7, 8, 9);
        assertEquals(expectedOrder, bst.inOrder(), "中序遍历应为 [2, 3, 4, 5, 7, 8, 9]");
    }

    // 测试后序遍历
    @Test
    public void testPostOrder() {
        List<Integer> expectedOrder = Arrays.asList(2, 4, 3, 7, 9, 8, 5);
        assertEquals(expectedOrder, bst.postOrder(), "后序遍历应为 [2, 4, 3, 7, 9, 8, 5]");
    }

    // 测试层序遍历
    @Test
    public void testLevelOrder() {
        List<Integer> expectedOrder = Arrays.asList(5, 3, 8, 2, 4, 7, 9);
        assertEquals(expectedOrder, bst.levelOrder(), "层序遍历应为 [5, 3, 8, 2, 4, 7, 9]");
    }

    // 测试空树的遍历
    @Test
    public void testEmptyTreeTraversal() {
        BinarySearchTree emptyTree = new BinarySearchTree();

        // 空树的所有遍历结果都应为空列表
        assertTrue(emptyTree.levelOrder().isEmpty(), "空树的层序遍历应为空");
        assertTrue(emptyTree.preOrder().isEmpty(), "空树的前序遍历应为空");
        assertTrue(emptyTree.inOrder().isEmpty(), "空树的中序遍历应为空");
        assertTrue(emptyTree.postOrder().isEmpty(), "空树的后序遍历应为空");
    }
}

