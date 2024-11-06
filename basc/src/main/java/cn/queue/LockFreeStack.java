package cn.queue;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

class LockFreeStack<T> {
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

    public static void main(String[] args) throws InterruptedException {
        LockFreeStack<Integer> stack = new LockFreeStack<>();
        ExecutorService executorService = Executors.newFixedThreadPool(4);

        // 使用多个线程进行并发的 push 和 pop 操作
        for (int i = 0; i < 2; i++) {
            int threadNum = i;
            executorService.execute(() -> {
                for (int j = 0; j < 5; j++) {
                    int value = threadNum * 10 + j;
                    stack.push(value);
                    System.out.println("Thread " + threadNum + " pushed: " + value);
                }
            });
        }

        for (int i = 0; i < 2; i++) {
            int threadNum = i;
            executorService.execute(() -> {
                for (int j = 0; j < 5; j++) {
                    try {
                        Integer value = stack.pop();
                        System.out.println("Thread " + threadNum + " popped: " + value);
                    } catch (IllegalStateException e) {
                        System.out.println("Thread " + threadNum + " tried to pop from empty stack.");
                    }
                }
            });
        }

        // 关闭线程池并等待所有任务完成
        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.MINUTES);

        // 输出最终的栈状态
        System.out.println("Final Stack: " + stack.toString());
    }
}