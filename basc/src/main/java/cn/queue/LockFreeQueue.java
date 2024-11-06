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
    //移除并返回队首元素
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
