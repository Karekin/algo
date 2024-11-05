package cn.queue;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BlockingQueue<T> {
    private final Queue<T> queue = new LinkedList<>();
    private final int capacity;
    private final Lock lock = new ReentrantLock();
    private final Condition notFull = lock.newCondition();
    private final Condition notEmpty = lock.newCondition();

    public BlockingQueue(int capacity) {
        this.capacity = capacity;
    }

    public void push(T data) throws InterruptedException {
        lock.lock();
        try {
            while (queue.size() >= capacity) {
                System.out.println("Queue is full, blocking");
                notFull.await();
            }
            queue.add(data);
            notEmpty.signal();
        } finally {
            lock.unlock();
        }
    }

    public int size() {
        lock.lock();
        try {
            return queue.size();
        } finally {
            lock.unlock();
        }
    }

    public T pop() throws InterruptedException {
        lock.lock();
        try {
            while (queue.isEmpty()) {
                System.out.println("Queue is empty, blocking");
                notEmpty.await();
            }
            T value = queue.poll();
            notFull.signal();
            return value;
        } finally {
            lock.unlock();
        }
    }
}
