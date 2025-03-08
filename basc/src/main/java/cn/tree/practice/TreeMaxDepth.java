package cn.tree.practice;

/**
 *  二叉树的最大深度，通过：<a href="https://pythontutor.com/render.html#mode=display">Tutor</a> 查看动画演示
 */
public class TreeMaxDepth {

    public int maxDepth(TreeNode root) {
        if (root == null) {
            return 0;
        }
        int leftDepth = maxDepth(root.left);
        int rightDepth = maxDepth(root.right);
        return Math.max(leftDepth, rightDepth) + 1;
    }

    public int minDepth(TreeNode root) {
        if (root == null) {
            return 0;
        }

        int leftDepth = minDepth(root.left);
        int rightDepth = minDepth(root.right);

        /*
         * ⚠️如果某个节点的左子树或右子树为空，则不能简单地取左右深度的最小值。
         * 因为空节点对应的深度为0，但空节点并不是叶子节点。
         *
         * 例如：
         *    1
         *     \
         *      2
         * 此时节点1的最小深度应为2，而不是1。
         */
        if (root.left == null || root.right == null) {
            return leftDepth + rightDepth + 1;
        } else {
            return Math.min(leftDepth, rightDepth) + 1;
        }
    }


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

    public static void main(String[] args) {
        TreeMaxDepth solution = new TreeMaxDepth();

        /*
          构建以下二叉树：
                  3
                 / \
                9  20
                   / \
                  15  7
        */
        TreeNode firstRoot = new TreeNode(3);
        firstRoot.left = new TreeNode(9);
        firstRoot.right = new TreeNode(20, new TreeNode(15), new TreeNode(7));

        System.out.println("最小深度: " + solution.maxDepth(firstRoot)); // 预期输出：3


        /*
          构建以下二叉树：
          [2,null,3,null,4,null,5,null,6]

          对应结构：
          2
           \
            3
             \
              4
               \
                5
                 \
                  6
        */
        TreeNode secondRoot = new TreeNode(2);
        secondRoot.right = new TreeNode(3);
        secondRoot.right.right = new TreeNode(4);
        secondRoot.right.right.right = new TreeNode(5);
        secondRoot.right.right.right.right = new TreeNode(6);

        System.out.println("最小深度: " + solution.minDepth(secondRoot)); // 预期输出：5
    }
}
