package cn.iocoder.boot.tree;

import cn.tree.AVLTree;
import cn.tree.AVLTree.TreeNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AVLTreeTest {
    private AVLTree avlTree;

    // 初始化 AVL 树实例
    @BeforeEach
    public void setUp() {
        avlTree = new AVLTree();
    }

    // 测试插入操作并验证平衡性
    @Test
    public void testInsertAndBalance() {
        avlTree.insert(10);
        avlTree.insert(20);
        avlTree.insert(30);
        avlTree.insert(40);
        avlTree.insert(50);
        avlTree.insert(25);

        // 中序遍历结果应为有序的 [10, 20, 25, 30, 40, 50]
        assertEquals("10 20 25 30 40 50", inOrderTraversal(avlTree.getRoot()), "插入后中序遍历应为 [10, 20, 25, 30, 40, 50]");

        // 检查树的平衡性
        assertTrue(isBalanced(avlTree.getRoot()), "插入后树应保持平衡");
    }

    // 测试删除操作并验证平衡性
    @Test
    public void testRemoveAndBalance() {
        avlTree.insert(10);
        avlTree.insert(20);
        avlTree.insert(30);
        avlTree.insert(40);
        avlTree.insert(50);
        avlTree.insert(25);

        // 删除一个叶节点
        avlTree.remove(10);
        assertNull(avlTree.search(10), "删除节点 10 后应在树中查找不到该节点");

        // 删除一个具有单子节点的节点
        avlTree.remove(30);
        assertNull(avlTree.search(30), "删除节点 30 后应在树中查找不到该节点");

        // 中序遍历结果应为有序的 [20, 25, 40, 50]
        assertEquals("20 25 40 50", inOrderTraversal(avlTree.getRoot()), "删除节点后中序遍历应为 [20, 25, 40, 50]");

        // 检查树的平衡性
        assertTrue(isBalanced(avlTree.getRoot()), "删除节点后树应保持平衡");
    }

    // 测试查找操作
    @Test
    public void testSearch() {
        avlTree.insert(10);
        avlTree.insert(20);
        avlTree.insert(30);

        TreeNode result = avlTree.search(20);
        assertNotNull(result, "应能找到值为 20 的节点");
        assertEquals(20, result.val, "找到的节点值应为 20");

        assertNull(avlTree.search(40), "未插入的值 40 不应在树中找到");
    }

    // 测试空树的查找、插入和删除
    @Test
    public void testEmptyTreeOperations() {
        assertNull(avlTree.search(10), "空树中不应找到任何节点");

        avlTree.insert(10);
        assertNotNull(avlTree.search(10), "插入值 10 后应能在树中找到该值");

        avlTree.remove(10);
        assertNull(avlTree.search(10), "删除根节点后树中不应再包含该值");
    }

    // 辅助方法：获取中序遍历的结果，用于验证树的结构
    private String inOrderTraversal(TreeNode node) {
        StringBuilder result = new StringBuilder();
        inOrderHelper(node, result);
        return result.toString().trim();
    }

    private void inOrderHelper(TreeNode node, StringBuilder result) {
        if (node == null) return;
        inOrderHelper(node.left, result);
        result.append(node.val).append(" ");
        inOrderHelper(node.right, result);
    }

    // 辅助方法：验证树是否平衡
    private boolean isBalanced(TreeNode node) {
        if (node == null) return true;
        int balanceFactor = Math.abs(avlTree.balanceFactor(node));
        return balanceFactor <= 1 && isBalanced(node.left) && isBalanced(node.right);
    }
}
