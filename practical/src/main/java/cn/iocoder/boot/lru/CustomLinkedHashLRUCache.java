package cn.iocoder.boot.lru;

import java.util.HashMap;
import java.util.Map;

/**
 * <p><strong>手动实现双向链表 + 哈希表：</strong></p>
 * <p>使用手动维护的双向链表和哈希表。自定义节点和哨兵节点管理双向链表。</p>
 *
 * <h2>难点1: 代码并没有主动维护 dummy 的 prev 指针指向尾节点，这一点是如何体现的？</h2>
 * <p>从表面看，确实没有单独出现 <code>dummy.prev = xxx;</code> 之类的赋值语句，<br>
 * 但实际上 <strong>dummy.prev 指向尾节点</strong> 是靠“环状结构”被<strong>自动维护</strong>的。</p>
 *
 * <hr>
 *
 * <h2>为什么会自动维护？</h2>
 *
 * <h3>1. 链表是环状的</h3>
 * <p>在构造函数里，你可以看到：</p>
 * <pre>
 * dummy.prev = dummy;
 * dummy.next = dummy;
 * </pre>
 * <p>这意味着最初链表是一个空的环：<code>dummy</code> 自己前驱和后继都指向自己。<br>
 * 当你插入节点时，节点之间也跟 <code>dummy</code> 一样是双向相互指向的，它们彼此连成一圈。</p>
 *
 * <h3>2. 删除节点时的双向链接更新</h3>
 * <p><code>remove(x)</code> 方法中只有两行：</p>
 * <pre>
 * x.prev.next = x.next;
 * x.next.prev = x.prev;
 * </pre>
 * <p>这两句就足够让整条链上的相邻指针自动串起来：</p>
 * <ul>
 *     <li>若 <code>x</code> 是尾节点（也就是 <code>dummy.prev</code>），则它的 <code>x.next</code> 会是 <code>dummy</code>；
 *     那么执行完 <code>x.next.prev = x.prev;</code> 等价于 <code>dummy.prev = x.prev;</code>。</li>
 *     <li>反之亦然，如果 <code>x</code> 是头节点、或中间节点，也会通过这两行逻辑把指针更新正确。</li>
 * </ul>
 *
 * <h3>3. 添加节点时的双向链接更新</h3>
 * <p><code>pushFront(x)</code> 方法是这样写的：</p>
 * <pre>
 * x.prev = dummy;
 * x.next = dummy.next;
 * x.prev.next = x;
 * x.next.prev = x;
 * </pre>
 * <p>依旧是利用相邻节点的前驱、后继指针来“自动连环”，无需显式地去设定谁是“尾节点”。</p>
 *
 * <p>由于<strong>所有节点都按双向环的形式连接</strong>，所以 <code>dummy</code> 的 <code>prev</code> 总是能在每次插入或删除后自动更新为环中的最后一个节点（尾节点）。<br>
 * 之所以不需要显式写 <code>dummy.prev = tailNode;</code>，就是因为那两行 <code>x.prev.next = x.next;</code> 和 <code>x.next.prev = x.prev;</code> 已经根据环状结构，
 * 把 <code>dummy</code> 与其他节点的前后关系维护好了。</p>
 *
 * <hr>
 *
 * <h2>简要总结</h2>
 * <ul>
 *     <li>这是一个 <strong>双向循环链表</strong> 的实现：<code>dummy</code> 既是哨兵节点，又是链表的<strong>环入口</strong>。</li>
 *     <li>当你插入或删除节点时，<strong>通过一前一后的指针相互更新</strong>，就能保持整条链的正确结构；</li>
 *     <li><strong>没有显式代码</strong>维护 <code>dummy.prev</code> 指向尾节点，是因为<strong>环状结构</strong>让它在“邻居指针更新”时天然被修正。</li>
 * </ul>
 *
 * <p>因此，<code>dummy.prev</code> 最终总是指向链表尾部节点，这就是双向循环链表的妙处所在。</p>
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

    /**
     *        dummy
     *         /  \
     *        v    ^
     *   [head] <-- [tail]
     *     ^         |
     *     |         |
     *     +---------+
     * @param capacity
     */
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
