package cn.iocoder.boot.queue;

import cn.queue.LockFreeQueue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

public class LockFreeQueueTest {

    private LockFreeQueue<Integer> queue;

    @BeforeEach
    public void setUp() {
        queue = new LockFreeQueue<>();
    }

    @Test
    public void testAddLastAndRemoveFirst() {
        // 测试添加和移除单个元素
        queue.addLast(1);
        assertEquals(1, queue.removeFirst());
    }

    @Test
    public void testRemoveFromEmptyQueue() {
        // 测试从空队列移除元素应返回null
        assertNull(queue.removeFirst());
    }

    @Test
    public void testAddNullElement() {
        // 测试添加null元素时抛出异常
        assertThrows(NullPointerException.class, () -> queue.addLast(null));
    }

    @Test
    public void testSequentialAddAndRemove() {
        // 测试顺序添加和移除多个元素
        queue.addLast(1);
        queue.addLast(2);
        queue.addLast(3);

        assertEquals(1, queue.removeFirst());
        assertEquals(2, queue.removeFirst());
        assertEquals(3, queue.removeFirst());
    }

    @Test
    public void testSizeAfterOperations() {
        // 测试执行若干操作后队列的大小
        assertEquals(0, queue.size());

        queue.addLast(1);
        queue.addLast(2);
        assertEquals(2, queue.size());

        queue.removeFirst();
        assertEquals(1, queue.size());

        queue.removeFirst();
        assertEquals(0, queue.size());
    }

    /**
     * 要证明一个无锁队列（如 LockFreeQueue）是线程安全的，
     * 通常可以通过并发测试来观察其在多线程环境下是否能够正确地执行入队和出队操作，
     * 且不会出现数据丢失、重复或数据不一致的情况。

     * 我们可以使用类似以下的测试方法：
     * 1、并发写入和读取：使用多个线程同时向队列中写入和读取数据，记录写入的数据并验证读取的数据。
     * 2、元素唯一性验证：借助 ConcurrentHashMap 或 Set 来记录已读取的元素，确保没有重复。
     * 3、数量一致性验证：在所有线程结束后，检查队列的大小是否与预期一致。
     * @throws InterruptedException
     */
    @Test
    public void testCustomThreadSafeQueue() throws InterruptedException {
        // 创建线程池
        ThreadPoolExecutor threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
        LockFreeQueue<String> queue = new LockFreeQueue<>();
        ConcurrentHashMap<String, Object> map = new ConcurrentHashMap<>();
        Random random = new Random(System.currentTimeMillis());

        // 创建5个写线程，每个线程向队列添加10个元素；IntStream.range(0, 5) 会创建一个从 0 到 4 的整数流
        IntStream.range(0, 5).forEach(i -> threadPool.submit(() -> {
            int count = 0;
            while (count++ < 10) {
                String element = System.nanoTime() + ":" + random.nextInt(10000) + ":" + count;
                queue.addLast(element);
            }
        }));

        // 创建5个读线程，每个线程从队列移除10个元素
        IntStream.range(0, 5).forEach(i -> threadPool.submit(() -> {
            int count = 10;
            while (count-- > 0) {
                try {
                    TimeUnit.MILLISECONDS.sleep(20);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                String result = queue.removeFirst();
                if (result != null) {
                    map.put(result, new Object());
                }
            }
        }));

        // 关闭线程池并等待所有任务完成
        threadPool.shutdown();
        threadPool.awaitTermination(1, TimeUnit.HOURS);

        // 验证队列中的元素数与写入的总数是否一致
        assertEquals(50, map.size(), "Map should contain exactly 50 unique elements.");
    }

    /**
     * 同样的方法可以用于测试非线程安全的队列，测试结果很可能会显示以下问题：

     * 丢失数据：某些写入的数据可能在并发读取时未能被读取，导致数据丢失。
     * 数据重复：多个线程可能同时访问并修改共享状态，导致读取到重复的数据。
     * 抛出异常：在激烈的并发操作下，非线程安全的队列可能抛出异常。
     * @throws InterruptedException
     */
    @Test
    public void testNonThreadSafeQueue() throws InterruptedException {
        // 创建线程池
        ThreadPoolExecutor threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
        // 使用线程不安全的ArrayDeque
        ArrayDeque<String> queue = new ArrayDeque<>();
        ConcurrentHashMap<String, Object> map = new ConcurrentHashMap<>();
        Random random = new Random(System.currentTimeMillis());

        // 创建5个写线程，每个线程向队列添加10个元素；IntStream.range(0, 5) 会创建一个从 0 到 4 的整数流
        IntStream.range(0, 5).forEach(i -> threadPool.submit(() -> {
            int count = 0;
            while (count++ < 10) {
                String element = System.nanoTime() + ":" + random.nextInt(10000) + ":" + count;
                queue.addLast(element);
            }
        }));

        // 创建5个读线程，每个线程从队列移除10个元素
        IntStream.range(0, 5).forEach(i -> threadPool.submit(() -> {
            int count = 10;
            while (count-- > 0) {
                try {
                    TimeUnit.MILLISECONDS.sleep(20);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                String result = queue.removeFirst();
                if (result != null) {
                    map.put(result, new Object());
                }
            }
        }));

        // 关闭线程池并等待所有任务完成
        threadPool.shutdown();
        threadPool.awaitTermination(1, TimeUnit.HOURS);

        // 验证队列中的元素数与写入的总数是否一致
        assertEquals(50, map.size(), "Map should contain exactly 50 unique elements.");
    }

    /**
     * 学习官方的线程安全队列：ConcurrentLinkedQueue
     * @throws InterruptedException
     */
    @Test
    public void testOfficialThreadSafeQueue() throws InterruptedException {
        ThreadPoolExecutor threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
        ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<>();
        ConcurrentHashMap<String, Object> map = new ConcurrentHashMap<>();

        // 创建5个写线程，每个线程添加10个元素
        IntStream.range(0, 5).forEach(i -> threadPool.submit(() -> {
            IntStream.rangeClosed(1, 10).forEach(count -> {
                String element = System.nanoTime() + ":" + ThreadLocalRandom.current().nextInt(10000) + ":" + count;
                queue.add(element);
            });
        }));

        // 创建5个读线程，每个线程读取10个元素
        IntStream.range(0, 5).forEach(i -> threadPool.submit(() -> {
            IntStream.rangeClosed(1, 10).forEach(count -> {
                try {
                    TimeUnit.MILLISECONDS.sleep(20);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                String result = queue.poll();
                if (result != null) {
                    map.put(result, new Object());
                }
            });
        }));

        // 关闭线程池并等待所有任务完成
        threadPool.shutdown();
        threadPool.awaitTermination(1, TimeUnit.HOURS);

        // 验证写入和读取的元素数量是否一致
        assertEquals(50, map.size(), "Map should contain exactly 50 unique elements.");
    }

}
