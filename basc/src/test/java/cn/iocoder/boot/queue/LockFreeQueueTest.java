package cn.iocoder.boot.queue;

import cn.queue.LockFreeQueue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

public class LockFreeQueueTest {

    private LockFreeQueue<Integer> queue;

    private static final int THREAD_COUNT = 20;
    private static final int ELEMENTS_PER_THREAD = 500;

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

    // 测试 LockFreeQueue 中 Node 的 volatile 字段的作用（需要提前去掉 volatile 字段）
    @RepeatedTest(20) // 重复运行多次，增大出错几率
    public void testConcurrentAddAndRemoveWithoutVolatile() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT * 2);
        AtomicInteger addCounter = new AtomicInteger(0);

        // 启动添加线程
        for (int i = 0; i < THREAD_COUNT; i++) {
            new Thread(() -> {
                for (int j = 0; j < ELEMENTS_PER_THREAD; j++) {
                    queue.addLast(addCounter.incrementAndGet());
                }
                latch.countDown();
            }).start();
        }

        // 启动移除线程
        AtomicInteger removeCounter = new AtomicInteger(0);
        for (int i = 0; i < THREAD_COUNT; i++) {
            new Thread(() -> {
                for (int j = 0; j < ELEMENTS_PER_THREAD; j++) {
                    Integer value = queue.removeFirst();
                    if (value != null) {
                        removeCounter.incrementAndGet();
                    }
                }
                latch.countDown();
            }).start();
        }

        // 等待所有线程执行完毕
        latch.await();

        // 验证添加和移除的计数是否一致
        /*
            在测试代码中，仅验证 addCounter 和 removeCounter 的计数差，而不直接验证 size，理由如下：
            1. size 变量的更新问题
                即便 size 是 AtomicInteger，由于 size 的递增和递减操作并没有直接绑定到 addLast 和 removeFirst 的操作成功与否上，
                可能会导致计数不准确。也就是说，size 并不能准确地反映当前队列的状态，尤其是在并发操作中，addLast 和 removeFirst 可能会互相影响。
                在无锁结构中，size 实际上很难准确维护，因此，通常在无锁队列的实现中，不建议直接依赖 size 来验证队列元素的数量。
            2. removeFirst 中的竞争条件
                当前的实现中，如果两个线程几乎同时执行 removeFirst 操作，它们可能会先后读取 head，并都尝试更新 head 的值。
                由于 head.compareAndSet 是乐观锁，即使 valueNode 为 null，size.decrementAndGet() 依然会执行，从而导致 size 计数错误。
            3. 队列为空时的多次调用 removeFirst 会导致 size 负数
                如果多个线程调用 removeFirst 时，valueNode == null 时继续执行 size.decrementAndGet()，
                这会导致 size 变成负数，这是由于在并发情况下，多个线程看到的 head.next 都是 null，但依然会减少 size。
         */
        assertEquals(addCounter.get(), removeCounter.get(), "Mismatch between added and removed elements");
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
