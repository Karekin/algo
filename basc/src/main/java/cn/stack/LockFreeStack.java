package cn.stack;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class LockFreeStack<T> {
    private final AtomicReference<Node<T>> head = new AtomicReference<>(null);

    public static class Node<T> {
        T val;
        Node<T> next;

        Node(T v) {
            this.val = v;
        }
    }


    // 头插法
    public void push(T val) {
        Node<T> node = new Node<>(val);
        do {
            node.next = head.get();
        } while (!head.compareAndSet(node.next, node));
    }

    // 头部弹出
    public T pop() {
        Node<T> curr;
        do {
            curr = head.get();
            if (curr == null) {
                throw new IllegalStateException("Stack is empty");
            }
        } while (!head.compareAndSet(curr, curr.next));
        return curr.val;
    }

    public boolean empty() {
        return head.get() == null;
    }

    // 非线程安全的方法
    public String toString() {
        StringBuilder res = new StringBuilder();
        Node<T> it = head.get();
        while (it != null) {
            res.append(it.val).append("->");
            it = it.next;
        }
        res.append("null");
        return res.toString();
    }
}