package cn.queue;


import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

public class LockFreeQueue<E> {
    //定义头和尾的原子性节点
    private final AtomicReference<Node<E>> head;
    private final AtomicReference<Node<E>> last;
    //定义原子性size
    private final AtomicInteger size = new AtomicInteger(0);
    //初始化队列，将头和尾都指向一个null的节点
    public LockFreeQueue() {
        Node<E> node = new Node<E>(null);
        head = new AtomicReference<Node<E>>(node);
        last = new AtomicReference<Node<E>>(node);
    }
    //定义节点类
    private static class Node<E> {
        E element;
        //需要volatile，因为防止在next赋值的时候发生重排序，并且需要对其他线程可见
        volatile Node<E> next;

        public Node(E element) {
            this.element = element;
        }

        @Override
        public String toString() {
            return element == null ? null : element.toString();
        }
    }

    public int size() {
        return size.get();
    }

    //添加元素到队尾
    public void addLast(E element) {
        //元素不允许为null
        if (element == null)
            throw new NullPointerException("The null element not allow");
        //新建一个新节点
        Node<E> newNode = new Node<E>(element);
        //getAndSet操作为原子性操作，先获取last的节点再将新的节点赋值给last
        Node<E> oldNode = last.getAndSet(newNode);
        //将旧节点的next指向新的节点
        oldNode.next = newNode;
        //队列长度+1
        size.incrementAndGet();
    }
    /*
        移除并返回队首元素

        能否举例分析一下两个线程竞争的情况？
        好的，我们通过两个线程（假设为 `Thread A` 和 `Thread B`）竞争调用 `removeFirst` 方法的过程来分析，展示它们在操作中如何利用 CAS（Compare-And-Swap）来确保线程安全性。

        初始状态假设
            - 队列中有三个节点，分别是 `head -> node1 -> node2 -> node3`。
            - 其中，`head` 是一个空节点（不包含数据），实际的数据节点从 `node1` 开始。
            - `head` 当前指向 `node1`。

        两个线程的竞争示例
            1. `Thread A` 和 `Thread B` 都开始调用 `removeFirst`
            - `Thread A` 和 `Thread B` 同时执行 `removeFirst` 方法。
            - 两个线程几乎在同一时间获取了 `head` 和 `head.next`，因此：
              - `headNode` 在两个线程中都是 `head`。
              - `valueNode` 在两个线程中都是 `node1`（也就是实际存放数据的第一个节点）。
            2. `Thread A` 尝试 CAS 操作
            - `Thread A` 执行 `head.compareAndSet(headNode, valueNode)`，即尝试将 `head` 从 `headNode` 更新为 `valueNode`（即从 `head` 更新为 `node1`）。
            - 因为此时没有其他线程成功更新 `head`，所以 `compareAndSet` 操作成功。
            - `Thread A` 将 `head` 指向了 `node1`，并且读取 `node1.element` 作为返回值。
            - 为了避免垃圾引用，`Thread A` 将 `node1.element` 置为 `null`，然后 `size` 减 1。
            3. `Thread B` 的 CAS 操作失败
            - `Thread B` 在尝试执行 `head.compareAndSet(headNode, valueNode)` 时发现，`head` 已经不再指向 `headNode`（`head` 已经被 `Thread A` 更新为 `node1`）。
            - 因此，`compareAndSet` 返回 `false`，`Thread B` 的操作失败。
            - `Thread B` 进入下一轮循环，重新获取 `head` 和 `head.next`。
            - 此时：
              - `headNode` 是 `node1`，`valueNode` 是 `node2`。
            4. `Thread B` 成功获取 `node2`
            - `Thread B` 重新执行 `head.compareAndSet(headNode, valueNode)`，即尝试将 `head` 从 `node1` 更新为 `node2`。
            - 由于 `head` 当前指向 `node1`，所以 `compareAndSet` 成功。
            - `Thread B` 将 `head` 指向了 `node2`，并成功移除 `node2` 的数据作为返回值。
            - 然后，`Thread B` 将 `node2.element` 置为 `null`，并将 `size` 减 1。

        总结：在这个竞争示例中，
            - 线程 A 和线程 B 都尝试获取队列的第一个元素，但只有第一个成功调用 `compareAndSet` 的线程（即 `Thread A`）能成功获取并移除 `node1`。
            - 线程 B 由于 CAS 失败，无法直接操作 `node1`，而是重新获取最新的 `head`，确保它不会影响到 `Thread A` 的移除操作。
            - 最终，`Thread A` 和 `Thread B` 各自成功移除一个节点，且没有线程间的干扰。
            
            这种通过 CAS 实现的机制，保证了在无锁环境下队列操作的原子性，使多个线程可以安全地操作队列而不造成数据不一致。

     */
    public E removeFirst() {
        //因为队首节点是存在的，但是他可能没有下一个节点，所以需要一个valueNode来判断
        Node<E> headNode, valueNode;
        do {
            //获取到队首节点
            headNode = head.get();
            //判断下一个节点是否为null
            valueNode = headNode.next;
            //当valueNode不为null，并且headNode不等于队列的head节点时，代表该元素被别的线程拿走的，需要重新获取。
            //当headNode等于队列的head时则代表头元素没有被其他元素拿走，并将head节点替换为valueNode。
        } while (valueNode != null && !head.compareAndSet(headNode, valueNode));

        E result = valueNode != null ? valueNode.element : null;
        //valueNode的元素被拿走了，所有将其置为null
        if (valueNode != null) {
            valueNode.element = null;
        }
        //队列长度-1
        size.decrementAndGet();
        return result;
    }
}
