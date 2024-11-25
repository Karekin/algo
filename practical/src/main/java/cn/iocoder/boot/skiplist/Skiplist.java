package cn.iocoder.boot.skiplist;

import java.util.Random;

/**
 * 跳表的一种实现方法。
 * 暂时无法满足：跳表中存储的是正整数，并且存储的是不重复的。
 * 深入研究 ConcurrentSkipListMap 源码，会有很大收获
 * <p>
 * Author：ZHENG
 */
class Skiplist {
    // 定义跳表的最大层数为10
    int level = 10;

    // 定义跳表的节点类
    class Node {
        int val; // 节点的值
        Node[] ne = new Node[level]; // 存储当前节点在每一层的下一个节点，ne 是指针数组

        // 构造函数，初始化节点值
        Node(int _val) {
            val = _val;
        }
    }

    // 随机数生成器，用于决定插入节点时是否向上一层
    Random random = new Random();

    // 跳表的哨兵节点，值为-1，表示最小值
    Node he = new Node(-1);

    // 查找操作，找出每一层比目标值 t 严格小的最后一个节点，将其存储在 ns 数组中
    void find(int t, Node[] ns) {
        Node cur = he; // 从哨兵节点开始遍历
        // 从最高层（level-1）往下查找
        for (int i = level - 1; i >= 0; i--) {
            // 在当前层，找到最后一个比 t 小的节点
            while (cur.ne[i] != null && cur.ne[i].val < t) cur = cur.ne[i];
            // 记录当前层的这个节点到 ns 数组
            ns[i] = cur;
        }
    }

    // 查找目标值 t 是否存在于跳表中
    public boolean search(int t) {
        Node[] ns = new Node[level]; // 用于存储每一层比 t 小的最后一个节点
        find(t, ns); // 查找过程
        // 判断最底层（第0层）是否存在值等于 t 的节点
        return ns[0].ne[0] != null && ns[0].ne[0].val == t;
    }

    // 插入操作，将值 t 插入到跳表中
    public void add(int t) {
        Node[] ns = new Node[level]; // 用于存储每一层比 t 小的最后一个节点
        find(t, ns); // 查找 t 在各层的位置
        Node node = new Node(t); // 创建要插入的新节点
        // 从最底层开始插入节点，并以一定概率向上层插入
        for (int i = 0; i < level; i++) {
            // 在第 i 层，插入新节点
            node.ne[i] = ns[i].ne[i]; // 新节点的第 i 层下一个节点指向原先的下一个节点
            ns[i].ne[i] = node; // 原先节点的第 i 层下一个节点指向新节点
            // 如果随机数生成为 0，则停止向更高层插入
            if (random.nextInt(2) == 0) break;
        }
    }

    // 删除操作，删除跳表中值为 t 的节点
    public boolean erase(int t) {
        Node[] ns = new Node[level]; // 用于存储每一层比 t 小的最后一个节点
        find(t, ns); // 查找 t 在各层的位置
        Node node = ns[0].ne[0]; // 取最底层第一个大于等于 t 的节点
        // 如果该节点不存在，或值不等于 t，直接返回 false
        if (node == null || node.val != t) return false;
        // 从下往上删除节点
        for (int i = 0; i < level && ns[i].ne[i] == node; i++) {
            // 将第 i 层中指向该节点的指针，改为指向该节点的下一个节点
            ns[i].ne[i] = ns[i].ne[i].ne[i];
        }
        return true; // 删除成功返回 true
    }
}
