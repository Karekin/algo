package cn.tree.practice;

public class HasPathSum {

    public static class TreeNode {
        int val;
        TreeNode left;
        TreeNode right;

        TreeNode() {}

        TreeNode(int val) {
            this.val = val;
        }

        TreeNode(int val, TreeNode left, TreeNode right) {
            this.val = val;
            this.left = left;
            this.right = right;
        }
    }

    /**
     * 判断二叉树中是否存在从根节点到叶子节点的路径，使路径上所有节点值之和等于给定的目标值。
     *
     * class Solution {
     *     public boolean hasPathSum(TreeNode root, int targetSum) {
     *         if (root == null) {
     *             return false;
     *         }
     *         targetSum -= root.val;
     *         if (root.left == null && root.right == null) {
     *             return targetSum == 0;
     *         }
     *         return hasPathSum(root.left, targetSum) || hasPathSum(root.right, targetSum);
     *     }
     * }
     *
     * ⚠️ 原代码存在的问题：
     * 1. 叶子节点判断不正确：
     *    原代码将遇到 null 节点时判断 targetSum 是否为 0，这可能导致非叶子节点（只有单侧子树为空）被误判为叶子节点，从而产生错误结果。
     *
     * 2. 对 null 节点的处理不当：
     *    按照题意，当遇到 null 节点时，表示路径无效，应直接返回 false。
     *
     * ✅ 正确的做法：
     * 应在明确当前节点为叶子节点（左右子节点均为空）时，再检查 targetSum 是否等于 0。
     * 若当前节点为 null，应直接返回 false。
     *
     * @param root 二叉树的根节点
     * @param targetSum 目标路径和
     * @return 如果存在满足条件的路径返回 true，否则返回 false
     */
    public boolean hasPathSum(TreeNode root, int targetSum) {
        if (root == null) {
            return false;
        }

        targetSum -= root.val;

        // 到达叶子节点时检查路径和是否等于0
        if (root.left == null && root.right == null) {
            return targetSum == 0;
        }

        return hasPathSum(root.left, targetSum) || hasPathSum(root.right, targetSum);
    }

    public static void main(String[] args) {
        HasPathSum solution = new HasPathSum();

        // 测试用例: root = [], targetSum = 0
        TreeNode root = null; // 空树
        int targetSum = 0;

        boolean result = solution.hasPathSum(root, targetSum);

        System.out.println(result);  // 预期输出: false
    }
}
