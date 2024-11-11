package cn.tree;

import java.util.*;

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

    /* 使用栈进行前序遍历 */
    public List<Integer> preOrderWithDeque() {
        List<Integer> res = new ArrayList<>();
        if (root == null) return res;

        Stack<TreeNode> stack = new Stack<>();
        stack.push(root);

        while (!stack.isEmpty()) {
            TreeNode node = stack.pop();
            res.add(node.val);  // 先访问根节点

            if (node.right != null) stack.push(node.right);  // 右子节点先压栈
            if (node.left != null) stack.push(node.left);    // 左子节点后压栈
        }

        return res;
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

    /* 使用栈进行中序遍历 */
    public List<Integer> inOrderWithDeque() {
        List<Integer> res = new ArrayList<>();
        Deque<TreeNode> stack = new LinkedList<>();
        TreeNode current = root;

        while (current != null || !stack.isEmpty()) {
            // 1. 先将左节点链压入栈
            while (current != null) {
                stack.push(current);
                current = current.left;
            }
            // 2. 弹出栈顶节点，访问该节点
            current = stack.pop();
            res.add(current.val);
            // 3. 转向右子节点
            current = current.right;
        }

        return res;
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

    /* 使用栈进行后序遍历 */
    public List<Integer> postOrderWithDeque() {
        List<Integer> res = new ArrayList<>();
        if (root == null) return res;

        Stack<TreeNode> stack1 = new Stack<>();
        Stack<TreeNode> stack2 = new Stack<>();
        stack1.push(root);

        while (!stack1.isEmpty()) {
            TreeNode node = stack1.pop();
            stack2.push(node);  // 将节点压入第二个栈
            if (node.left != null) stack1.push(node.left);  // 左子节点压入
            if (node.right != null) stack1.push(node.right); // 右子节点压入
        }

        while (!stack2.isEmpty()) {
            res.add(stack2.pop().val);  // 第二个栈弹出即为「左 - 右 - 根」
        }

        return res;
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
