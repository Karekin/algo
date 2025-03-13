package cn.iocoder.boot.lru;

import java.util.HashMap;
import java.util.Map;

/**
 * 手动实现双向链表 + 哈希表：
 * 使用手动维护的双向链表和哈希表。
 * 自定义节点和哨兵节点管理双向链表。
 */
class CustomLinkedHashLRUCache {
    private static class Node {
        int key, value;
        Node prev, next;

        Node(int k, int v) {
            key = k;
            value = v;
        }
    }

    private final int capacity;
    private final Node dummy = new Node(0, 0); // 哨兵节点
    private final Map<Integer, Node> keyToNode = new HashMap<>();

    public CustomLinkedHashLRUCache(int capacity) {
        this.capacity = capacity;
        dummy.prev = dummy;
        dummy.next = dummy;
    }

    public int get(int key) {
        Node node = getNode(key);
        return node != null ? node.value : -1;
    }

    public void put(int key, int value) {
        Node node = getNode(key);
        if (node != null) { // 有这本书
            node.value = value; // 更新 value
            return;
        }

        /**
         *         dummy
         *           │
         *           ▼
         *   ┌─────────┐   ┌─────────┐   ┌─────────┐
         *   │  Node1  │ ↔ │  Node2  │ ↔ │ NodeN  │  ← backNode（最后一本书）
         *   └─────────┘   └─────────┘   └─────────┘
         *       ↑                           ↑
         *  dummy.next                  dummy.prev
         *
         * 操作过程
         * 步骤1： 判断总数超过容量
         * 步骤2： 定位最后一本书 NodeN（通过 dummy.prev）
         * 步骤3： 删除 NodeN，即：
         * 从哈希表中删除 NodeN.key
         * 执行 remove(NodeN) 从链表中移除 NodeN
         */
        node = new Node(key, value); // 新书
        keyToNode.put(key, node);
        pushFront(node); // 放在最上面
        if (keyToNode.size() > capacity) { // 书太多了
            Node backNode = dummy.prev;
            keyToNode.remove(backNode.key);
            remove(backNode); // 去掉最后一本书
        }
    }

    // 获取 key 对应的节点，同时把该节点移到链表头部
    private Node getNode(int key) {
        if (!keyToNode.containsKey(key)) { // 没有这本书
            return null;
        }
        Node node = keyToNode.get(key); // 有这本书
        remove(node); // 把这本书抽出来
        pushFront(node); // 放在最上面
        return node;
    }

    // 删除一个节点（抽出一本书）

    /**
     *    Head
     *      ↓
     *    [ A ]  <==>  [ x ]  <==>  [ B ]
     *                       ↑
     *                     删除
     * @param x
     */
    private void remove(Node x) {
        x.prev.next = x.next;
        x.next.prev = x.prev;
    }

    // 在链表头添加一个节点（把一本书放在最上面）

    /**
     * 调用后（将 x 插入到链表头部）：
     *          dummy
     *            │
     *            ▼
     * +---------+   +---------+      +---------+
     * | dummy   |←→|    x    |←→ | Node1   |<--→ …
     * +---------+   +---------+      +---------+
     * 说明：pushFront(x) 的过程完成后，新节点 x 被插入在 dummy 后面，
     *      成为链表新的第一个节点，原先的 Node1 被移到 x 的后面。
     *
     * 这正是代码中四行语句的作用：
     * x.prev = dummy; —— 设置 x 的前驱为 dummy；
     * x.next = dummy.next; —— 设置 x 的后继为原来的第一个节点（Node1）；
     * x.prev.next = x; —— 使 dummy 的 next 指向 x；
     * x.next.prev = x; —— 更新原来第一个节点的 prev 为 x。
     */
    private void pushFront(Node x) {
        x.prev = dummy;
        x.next = dummy.next;
        x.prev.next = x;
        x.next.prev = x;
    }
}
