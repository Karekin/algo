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

    /*
        中序遍历要求先访问左子树，再访问根节点，最后访问右子树。
        这种顺序要求在遍历到每个节点时，需要先进入其左子树，而在访问右子树前要确保已经访问过左子树和根节点。
        为此，栈在这里的作用是「记录路径」，帮助我们找到回溯的点。

        while (current != null || !stack.isEmpty()) {
        - 这个循环保证了遍历整个树，直到所有节点都访问完毕。当 `current` 不为空时，表示我们正在处理一个节点；`stack` 不为空时，表示还有一些待回溯的节点。

        步骤 1：将左子树节点压入栈
        while (current != null) {
            stack.push(current);
            current = current.left;
        }
        - 含义：沿着左子树一路向下，将所有左子节点压入栈中。
        - 目的：保证在回溯到某个节点之前，已经完成对其左子树的访问。
        - 结束条件：`current` 为 `null`，即到达当前子树的最左节点。

        步骤 2：弹出栈顶节点，访问该节点
        current = stack.pop();
        res.add(current.val);
        - 含义：当没有更多左子节点时，弹出栈顶节点并访问它（即访问节点的值并加入结果列表）。
        - 原因：这表示该节点的左子树已访问完，现在可以处理该节点本身（符合中序遍历的「左 - 根」顺序）。

        步骤 3：转向右子节点
        current = current.right;
        - 含义：将 `current` 指向刚访问节点的右子节点。
        - 目的：开始处理右子树。之后会再次进入「步骤 1」，即左子节点压栈的过程。
        - 说明：如果右子节点为 `null`，则下一次循环会从栈中继续弹出，回溯到更上层的节点。

        假设树的结构如下：中序遍历的顺序应该是 `[4, 2, 5, 1, 3]`。
            1
           / \
          2   3
         / \
        4   5

        1. 初始状态：`current` 指向根节点 `1`，`stack` 为空。
        2. 步骤 1（向左子树遍历）：将 `1` 和 `2` 压入栈，最终 `current` 指向最左节点 `4`。
        3. 步骤 2（弹出和访问）：弹出并访问 `4`，加入到结果 `res`，`current` 转向 `null`。
        4. 步骤 3（回溯到上层）：`current` 为 `null`，因此弹出 `2`，加入结果，`current` 指向 `2` 的右子节点 `5`。
        5. 步骤 1 - 3（重复上述过程）：按「左 - 根 - 右」顺序继续，直到所有节点访问完毕。
     */
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
    /*
        stack1 的入栈顺序：根-左-右
        stack2 的入栈顺序：根-右-左
        stack2 的出栈顺序：左-右-根

        stack1的压栈顺序是「左子节点 -> 右子节点」，因为栈是后进先出（LIFO）的结构，在后续弹出时，右子节点会先于左子节点被处理。
        这正好符合我们需要的后序遍历顺序，最终在 `stack2` 中得到「根 -> 右 -> 左」的顺序。具体的原因如下：

        1. 先压左子节点，再压右子节点：
           - 当我们将左子节点先压入 `stack1` 后，右子节点后压入。
           - 在随后的弹出过程中，右子节点会先被弹出并处理，而左子节点稍后被弹出。
        2. `stack2` 中的顺序：
           - `stack1` 中的节点被弹出并压入 `stack2`，使得 `stack2` 最终保存的顺序是「根 -> 右 -> 左」。

       完整过程示例：以下是一个示例二叉树：
            1
           / \
          2   3
         / \
        4   5
        具体操作过程如下：
        1. 根节点 `1` 被压入 `stack1`。
        2. 循环 1：
           - 弹出 `1`，压入 `stack2`。
           - 压入 `1` 的左子节点 `2`，再压入右子节点 `3`。`stack1` 中现在的顺序是 `[3, 2]`。
        3. 循环 2：
           - 弹出 `3`，压入 `stack2`。`stack2` 现在是 `[1, 3]`。
           - `3` 没有子节点。
        4. 循环 3：
           - 弹出 `2`，压入 `stack2`。`stack2` 现在是 `[1, 3, 2]`。
           - 压入 `2` 的左子节点 `4`，再压入右子节点 `5`。`stack1` 中现在的顺序是 `[5, 4]`。
        5. 循环 4：
           - 弹出 `5`，压入 `stack2`。`stack2` 现在是 `[1, 3, 2, 5]`。
        6. 循环 5：
           - 弹出 `4`，压入 `stack2`。`stack2` 现在是 `[1, 3, 2, 5, 4]`。
        最终从 `stack2` 中弹出节点的顺序是 `[4, 5, 2, 3, 1]`，即符合后序遍历「左 -> 右 -> 根」的顺序。
     */
    public List<Integer> postOrderWithDeque() {
        List<Integer> res = new ArrayList<>();
        if (root == null) return res;
        /*
            stack1 入栈：根、左、右
            stack2 入栈: 根、右、左
            后序：左、右、根
         */
        Stack<TreeNode> stack1 = new Stack<>();
        Stack<TreeNode> stack2 = new Stack<>();
        /*
            在 Deque 中，add 和 push 方法的区别在于它们的添加位置和用法意图。

            1. push 方法
            push(E e)：将元素添加到栈的顶端（等效于队列的头部），也就是 Deque 的前端。
            push 方法实际上是专门用于模拟栈行为的方法，它相当于调用 addFirst(E e)。
            典型用途：用于实现栈的后进先出（LIFO）行为。
            2. add 方法
            add(E e)：默认将元素添加到 Deque 的尾端，等效于 addLast(E e)。
            add 方法是为队列行为设计的，通常用于实现先进先出（FIFO）结构。
            典型用途：用于实现队列的先进先出（FIFO）行为。
        */
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
