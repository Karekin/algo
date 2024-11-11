package cn.iocoder.boot.tree;

import cn.tree.LinkedListBinaryTree;
import cn.tree.LinkedListBinaryTree.TreeNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class LinkedListBinaryTreeTest {
    private LinkedListBinaryTree tree;

    // 初始化测试用的二叉树
    @BeforeEach
    public void setUp() {
        // 构建一个简单的二叉树
        //        1
        //       / \
        //      2   3
        //     / \
        //    4   5
        TreeNode root = new TreeNode(1);
        root.left = new TreeNode(2);
        root.right = new TreeNode(3);
        root.left.left = new TreeNode(4);
        root.left.right = new TreeNode(5);

        tree = new LinkedListBinaryTree(root);
    }

    // 测试层序遍历
    @Test
    public void testLevelOrder() {
        List<Integer> expectedOrder = Arrays.asList(1, 2, 3, 4, 5);
        assertEquals(expectedOrder, tree.levelOrder(), "层序遍历应为 [1, 2, 3, 4, 5]");
    }

    // 测试前序遍历
    @Test
    public void testPreOrder() {
        List<Integer> expectedOrder = Arrays.asList(1, 2, 4, 5, 3);
        assertEquals(expectedOrder, tree.preOrder(), "前序遍历应为 [1, 2, 4, 5, 3]");
    }

    // 测试中序遍历
    @Test
    public void testInOrder() {
        List<Integer> expectedOrder = Arrays.asList(4, 2, 5, 1, 3);
        assertEquals(expectedOrder, tree.inOrder(), "中序遍历应为 [4, 2, 5, 1, 3]");
    }

    // 测试后序遍历
    @Test
    public void testPostOrder() {
        List<Integer> expectedOrder = Arrays.asList(4, 5, 2, 3, 1);
        assertEquals(expectedOrder, tree.postOrder(), "后序遍历应为 [4, 5, 2, 3, 1]");
    }

    // 测试空树的遍历
    @Test
    public void testEmptyTreeTraversal() {
        LinkedListBinaryTree emptyTree = new LinkedListBinaryTree(null);

        // 空树的所有遍历结果都应为空列表
        assertTrue(emptyTree.levelOrder().isEmpty(), "空树的层序遍历应为空");
        assertTrue(emptyTree.preOrder().isEmpty(), "空树的前序遍历应为空");
        assertTrue(emptyTree.inOrder().isEmpty(), "空树的中序遍历应为空");
        assertTrue(emptyTree.postOrder().isEmpty(), "空树的后序遍历应为空");
    }

    // 测试使用栈的前序遍历
    @Test
    public void testPreOrderWithStack() {
        List<Integer> expectedOrder = Arrays.asList(1, 2, 4, 5, 3);
        assertEquals(expectedOrder, tree.preOrderWithDeque(), "栈实现的前序遍历应为 [1, 2, 4, 5, 3]");
    }

    // 测试使用栈的中序遍历
    @Test
    public void testInOrderWithStack() {
        List<Integer> expectedOrder = Arrays.asList(4, 2, 5, 1, 3);
        assertEquals(expectedOrder, tree.inOrderWithDeque(), "栈实现的中序遍历应为 [4, 2, 5, 1, 3]");
    }

    // 测试使用栈的后序遍历
    @Test
    public void testPostOrderWithStack() {
        List<Integer> expectedOrder = Arrays.asList(4, 5, 2, 3, 1);
        assertEquals(expectedOrder, tree.postOrderWithDeque(), "栈实现的后序遍历应为 [4, 5, 2, 3, 1]");
    }
}
