package cn.tree;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class LinkedListBinaryTree {
    TreeNode root;

    /* 构造方法，传入根节点 */
    public LinkedListBinaryTree(TreeNode root) {
        this.root = root;
    }

    /* 层序遍历 */
    public List<Integer> levelOrder() {
        List<Integer> result = new ArrayList<>();
        if (root == null) return result;

        Queue<TreeNode> queue = new LinkedList<>();
        queue.offer(root);
        while (!queue.isEmpty()) {
            TreeNode node = queue.poll();
            result.add(node.val);
            if (node.left != null) queue.offer(node.left);
            if (node.right != null) queue.offer(node.right);
        }
        return result;
    }

    /* 前序遍历 */
    public List<Integer> preOrder() {
        List<Integer> result = new ArrayList<>();
        preOrderHelper(root, result);
        return result;
    }

    private void preOrderHelper(TreeNode node, List<Integer> result) {
        if (node == null) return;
        result.add(node.val);               // 访问根节点
        preOrderHelper(node.left, result);   // 遍历左子树
        preOrderHelper(node.right, result);  // 遍历右子树
    }

    /* 中序遍历 */
    public List<Integer> inOrder() {
        List<Integer> result = new ArrayList<>();
        inOrderHelper(root, result);
        return result;
    }

    private void inOrderHelper(TreeNode node, List<Integer> result) {
        if (node == null) return;
        inOrderHelper(node.left, result);    // 遍历左子树
        result.add(node.val);                // 访问根节点
        inOrderHelper(node.right, result);   // 遍历右子树
    }

    /* 后序遍历 */
    public List<Integer> postOrder() {
        List<Integer> result = new ArrayList<>();
        postOrderHelper(root, result);
        return result;
    }

    private void postOrderHelper(TreeNode node, List<Integer> result) {
        if (node == null) return;
        postOrderHelper(node.left, result);  // 遍历左子树
        postOrderHelper(node.right, result); // 遍历右子树
        result.add(node.val);                // 访问根节点
    }

    public static class TreeNode {
        int val;
        public TreeNode left;
        public TreeNode right;

        public TreeNode(int val) {
            this.val = val;
            this.left = null;
            this.right = null;
        }
    }

}
