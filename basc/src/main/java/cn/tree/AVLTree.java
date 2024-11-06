package cn.tree;

public class AVLTree {
    private TreeNode root;

    /* AVL 树节点类 */
    public static class TreeNode {
        public int val;        // 节点值
        public int height;     // 节点高度
        public TreeNode left;  // 左子节点
        public TreeNode right; // 右子节点

        public TreeNode(int x) {
            val = x;
            height = 1; // 新节点初始高度为 1
        }
    }

    /* 获取节点高度 */
    private int height(TreeNode node) {
        return node == null ? 0 : node.height;
    }

    /* 更新节点高度 */
    private void updateHeight(TreeNode node) {
        node.height = 1 + Math.max(height(node.left), height(node.right));
    }

    /* 获取平衡因子 */
    public int balanceFactor(TreeNode node) {
        return node == null ? 0 : height(node.left) - height(node.right);
    }

    /* 右旋操作 */
    private TreeNode rightRotate(TreeNode node) {
        TreeNode child = node.left;
        TreeNode grandChild = child.right;
        // 以 child 为原点，将 node 向右旋转
        child.right = node;
        node.left = grandChild;
        // 更新节点高度
        updateHeight(node);
        updateHeight(child);
        // 返回旋转后子树的根节点
        return child;
    }

    /* 左旋操作 */
    private TreeNode leftRotate(TreeNode node) {
        TreeNode child = node.right;
        TreeNode grandChild = child.left;
        // 以 child 为原点，将 node 向左旋转
        child.left = node;
        node.right = grandChild;
        // 更新节点高度
        updateHeight(node);
        updateHeight(child);
        // 返回旋转后子树的根节点
        return child;
    }

    /* 执行旋转操作，使该子树重新恢复平衡 */
    private TreeNode rotate(TreeNode node) {
        int balanceFactor = balanceFactor(node);
        // 左偏树
        if (balanceFactor > 1) {
            if (balanceFactor(node.left) >= 0) {
                // 右旋
                return rightRotate(node);
            } else {
                // 先左旋后右旋
                node.left = leftRotate(node.left);
                return rightRotate(node);
            }
        }
        // 右偏树
        if (balanceFactor < -1) {
            if (balanceFactor(node.right) <= 0) {
                // 左旋
                return leftRotate(node);
            } else {
                // 先右旋后左旋
                node.right = rightRotate(node.right);
                return leftRotate(node);
            }
        }
        // 平衡树，无须旋转
        return node;
    }

    /* 插入节点 */
    public void insert(int val) {
        root = insertHelper(root, val);
    }

    /* 递归插入节点（辅助方法） */
    private TreeNode insertHelper(TreeNode node, int val) {
        if (node == null) {
            return new TreeNode(val);
        }
        // 1. 查找插入位置并插入节点
        if (val < node.val) {
            node.left = insertHelper(node.left, val);
        } else if (val > node.val) {
            node.right = insertHelper(node.right, val);
        } else {
            return node; // 重复节点不插入
        }
        updateHeight(node); // 更新节点高度
        // 2. 执行旋转操作，使该子树重新恢复平衡
        return rotate(node);
    }

    /* 删除节点 */
    public void remove(int val) {
        root = removeHelper(root, val);
    }

    /* 递归删除节点（辅助方法） */
    private TreeNode removeHelper(TreeNode node, int val) {
        if (node == null) return null;

        // 1. 查找节点并删除
        if (val < node.val) {
            node.left = removeHelper(node.left, val);
        } else if (val > node.val) {
            node.right = removeHelper(node.right, val);
        } else {
            if (node.left == null || node.right == null) {
                TreeNode child = (node.left != null) ? node.left : node.right;
                if (child == null) {
                    return null; // 子节点数量 = 0
                } else {
                    node = child; // 子节点数量 = 1
                }
            } else {
                // 子节点数量 = 2 ，则找到中序遍历的下一个节点
                TreeNode temp = node.right;
                while (temp.left != null) {
                    temp = temp.left;
                }
                node.val = temp.val;
                node.right = removeHelper(node.right, temp.val);
            }
        }
        updateHeight(node); // 更新节点高度
        // 2. 执行旋转操作，使该子树重新恢复平衡
        return rotate(node);
    }

    /* 查找节点 */
    public TreeNode search(int num) {
        TreeNode cur = root;
        while (cur != null) {
            if (cur.val < num) {
                cur = cur.right;
            } else if (cur.val > num) {
                cur = cur.left;
            } else {
                break;
            }
        }
        return cur;
    }

    /* 前序遍历 */
    public void preOrder(TreeNode node) {
        if (node == null) return;
        System.out.print(node.val + " ");
        preOrder(node.left);
        preOrder(node.right);
    }

    /* 中序遍历 */
    public void inOrder(TreeNode node) {
        if (node == null) return;
        inOrder(node.left);
        System.out.print(node.val + " ");
        inOrder(node.right);
    }

    /* 后序遍历 */
    public void postOrder(TreeNode node) {
        if (node == null) return;
        postOrder(node.left);
        postOrder(node.right);
        System.out.print(node.val + " ");
    }

    /* 获取根节点 */
    public TreeNode getRoot() {
        return root;
    }
}

