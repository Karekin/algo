package cn.queue;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BlockingQueue<T> {
    // 使用LinkedList作为队列存储元素
    private final Queue<T> queue = new LinkedList<>();
    // 队列的最大容量
    private final int capacity;
    // 可重入锁，用于保护队列的并发访问
    private final Lock lock = new ReentrantLock();
    // 条件变量，表示队列未满，用于控制生产者等待
    private final Condition notFull = lock.newCondition();
    // 条件变量，表示队列非空，用于控制消费者等待
    private final Condition notEmpty = lock.newCondition();

    // 构造方法，初始化阻塞队列的容量
    public BlockingQueue(int capacity) {
        this.capacity = capacity;
    }

    // push方法用于向队列中插入元素，当队列满时阻塞等待
    public void push(T data) throws InterruptedException {
        lock.lock();  // 加锁，确保线程安全
        try {
            // 当队列已满，生产者等待
            while (queue.size() >= capacity) {
                System.out.println("Queue is full, blocking");  // 输出提示信息
                notFull.await();  // 等待队列未满的信号
            }
            // 队列未满时，将元素加入队列
            queue.add(data);
            // 唤醒一个等待队列非空的消费者线程
            notEmpty.signal();
        } finally {
            lock.unlock();  // 解锁，释放锁资源
        }
    }

    // size方法返回队列的当前大小
    public int size() {
        lock.lock();  // 加锁，确保线程安全
        try {
            return queue.size();
        } finally {
            lock.unlock();  // 解锁，释放锁资源
        }
    }

    // pop方法用于从队列中取出元素，当队列为空时阻塞等待
    public T pop() throws InterruptedException {
        lock.lock();  // 加锁，确保线程安全
        try {
            // 当队列为空，消费者等待
            while (queue.isEmpty()) {
                System.out.println("Queue is empty, blocking");  // 输出提示信息
                notEmpty.await();  // 等待队列非空的信号
            }
            // 队列非空时，取出队列头部元素
            T value = queue.poll();
            // 唤醒一个等待队列未满的生产者线程
            notFull.signal();
            return value;  // 返回取出的元素
        } finally {
            lock.unlock();  // 解锁，释放锁资源
        }
    }
}
