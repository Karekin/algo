package cn.tree;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class BinarySearchTree {
    TreeNode root;

    /* 查找节点 */
    public TreeNode search(int num) {
        TreeNode cur = root;
        // 循环查找，越过叶节点后跳出
        while (cur != null) {
            // 目标节点在 cur 的右子树中
            if (cur.val < num)
                cur = cur.right;
                // 目标节点在 cur 的左子树中
            else if (cur.val > num)
                cur = cur.left;
                // 找到目标节点，跳出循环
            else
                break;
        }
        // 返回目标节点
        return cur;
    }

    /* 插入节点 */
    public void insert(int num) {
        // 若树为空，则初始化根节点
        if (root == null) {
            root = new TreeNode(num);
            return;
        }
        TreeNode cur = root, pre = null;
        // 循环查找，越过叶节点后跳出
        while (cur != null) {
            // 找到重复节点，直接返回
            if (cur.val == num)
                return;
            pre = cur;
            // 插入位置在 cur 的右子树中
            if (cur.val < num)
                cur = cur.right;
                // 插入位置在 cur 的左子树中
            else
                cur = cur.left;
        }
        // 插入节点
        TreeNode node = new TreeNode(num);
        if (pre.val < num)
            pre.right = node;
        else
            pre.left = node;
    }

    /* 删除节点 */
    public void remove(int num) {
        // 若树为空，直接提前返回
        if (root == null)
            return;
        TreeNode cur = root, pre = null;
        // 循环查找，越过叶节点后跳出
        while (cur != null) {
            // 找到待删除节点，跳出循环
            if (cur.val == num)
                break;
            pre = cur;
            // 待删除节点在 cur 的右子树中
            if (cur.val < num)
                cur = cur.right;
                // 待删除节点在 cur 的左子树中
            else
                cur = cur.left;
        }
        // 若无待删除节点，则直接返回
        if (cur == null)
            return;
        // 子节点数量 = 0 or 1
        if (cur.left == null || cur.right == null) {
            // 当子节点数量 = 0 / 1 时， child = null / 该子节点
            TreeNode child = cur.left != null ? cur.left : cur.right;
            // 删除节点 cur
            if (cur != root) {
                if (pre.left == cur)
                    pre.left = child;
                else
                    pre.right = child;
            } else {
                // 若删除节点为根节点，则重新指定根节点
                root = child;
            }
        }
        // 子节点数量 = 2
        else {
            // 获取中序遍历中 cur 的下一个节点
            TreeNode tmp = cur.right;
            while (tmp.left != null) {
                tmp = tmp.left;
            }
            // 递归删除节点 tmp
            remove(tmp.val);
            // 用 tmp 覆盖 cur
            cur.val = tmp.val;
        }
    }

    /* 前序遍历 */
    public List<Integer> preOrder() {
        List<Integer> result = new ArrayList<>();
        preOrderHelper(root, result);
        return result;
    }

    private void preOrderHelper(TreeNode node, List<Integer> result) {
        if (node == null) return;
        result.add(node.val);
        preOrderHelper(node.left, result);
        preOrderHelper(node.right, result);
    }

    /* 中序遍历 */
    public List<Integer> inOrder() {
        List<Integer> result = new ArrayList<>();
        inOrderHelper(root, result);
        return result;
    }

    private void inOrderHelper(TreeNode node, List<Integer> result) {
        if (node == null) return;
        inOrderHelper(node.left, result);
        result.add(node.val);
        inOrderHelper(node.right, result);
    }

    /* 后序遍历 */
    public List<Integer> postOrder() {
        List<Integer> result = new ArrayList<>();
        postOrderHelper(root, result);
        return result;
    }

    private void postOrderHelper(TreeNode node, List<Integer> result) {
        if (node == null) return;
        postOrderHelper(node.left, result);
        postOrderHelper(node.right, result);
        result.add(node.val);
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

    // 是否是有效的二叉搜索树（前序遍历卡范围）
    public boolean isValidBSTPreOrder() {
        /*
         * 方法采用递归方式判断每个节点是否在给定范围内：
         *
         * return recur(root, Integer.MIN_VALUE, Integer.MAX_VALUE);
         *
         * 整体思路正确，但使用 Integer.MIN_VALUE 和 Integer.MAX_VALUE 作为上下界时会出现问题：
         * 如果树中节点的值恰好等于 Integer.MIN_VALUE 或 Integer.MAX_VALUE，将被错误判定为无效的二叉搜索树。
         *
         * 例如，以下二叉树：
         *
         *     Integer.MAX_VALUE
         *        /
         * Integer.MIN_VALUE
         *
         * 按照原有代码逻辑：
         * - 根节点为 Integer.MAX_VALUE，左节点为 Integer.MIN_VALUE。
         * - 检测左子树时范围为 (Integer.MIN_VALUE, Integer.MAX_VALUE)，但左节点的值等于下限 Integer.MIN_VALUE，
         *   导致节点验证失败（val > left 不成立，因 val 等于边界值）。
         *
         * 实际上，该树是合法的 BST（左子树节点严格小于根节点，符合 BST 定义）。
         */

        return recur(root, Long.MIN_VALUE, Long.MAX_VALUE);
    }

    boolean recur(TreeNode root, long left, long right) {
        if (root == null) {
            return true;
        }
        int val = root.val;
        return val > left && val < right && recur(root.left, left, val) && recur(root.right, val, right);
    }


    private long pre = Long.MIN_VALUE;
    public boolean isValidBSTInOrder(TreeNode root) {
        if (root == null) {
            return true;
        }
        if (!isValidBSTInOrder(root.left)) {
            return false;
        }
        if (pre >= root.val) {
            return false;
        }
        pre = root.val;
        return isValidBSTInOrder(root.right);
    }


    // 树节点类
    public static class TreeNode {
        public int val;
        TreeNode left, right;
        TreeNode(int val) {
            this.val = val;
        }
    }
}

