package cn.iocoder.boot.queue;

import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * 单元测试类，用于测试 {@link FutureCompletingBlockingQueue} 的行为，覆盖各种场景：
 * - 基础功能（入队、出队）
 * - 容量约束（队列满、队列空）
 * - 并发安全（多个线程生产和消费）
 * - 线程唤醒（生产者在队列满时阻塞）
 */
public class FutureCompletingBlockingQueueTest {

    private static final int DEFAULT_CAPACITY = 256; // 默认容量，保持与无参构造函数一致

    /**
     * 测试基本功能：
     * - 入队、出队
     * - 检查队列大小、可用容量
     * - 验证 `getAvailabilityFuture()` 是否正确反映可用状态
     */
    @Test
    public void testBasics() throws InterruptedException {
        FutureCompletingBlockingQueue<Integer> queue = new FutureCompletingBlockingQueue<>(5);

        CompletableFuture<Void> future = queue.getAvailabilityFuture();
        assertThat(queue.isEmpty()).isTrue();
        assertThat(queue.size()).isEqualTo(0);

        queue.put(0, 1234);

        // 由于成功入队，之前获取的 future 应该被完成
        assertThat(future.isDone()).isTrue();
        assertThat(queue.size()).isEqualTo(1);
        assertThat(queue.isEmpty()).isFalse();
        assertThat(queue.remainingCapacity()).isEqualTo(4);
        assertThat(queue.peek()).isNotNull();
        assertThat(queue.peek()).isEqualTo(1234);

        int polled = queue.poll();
        assertThat(polled).isEqualTo(1234);

        // 确保出队后队列变为空
        assertThat(queue.size()).isEqualTo(0);
        assertThat(queue.isEmpty()).isTrue();
        assertThat(queue.remainingCapacity()).isEqualTo(5);
    }

    /**
     * 测试 `poll()` 方法是否能正确出队。
     */
    @Test
    public void testPoll() throws InterruptedException {
        FutureCompletingBlockingQueue<Integer> queue = new FutureCompletingBlockingQueue<>();
        queue.put(0, 1234);
        Integer value = queue.poll();
        assertThat(value).isNotNull();
        assertThat(value).isEqualTo(1234);
    }

    /**
     * 测试 `poll()` 在队列为空时返回 null。
     */
    @Test
    public void testPollEmptyQueue() throws InterruptedException {
        FutureCompletingBlockingQueue<Integer> queue = new FutureCompletingBlockingQueue<>();
        queue.put(0, 1234);

        assertThat(queue.poll()).isNotNull();
        assertThat(queue.poll()).isNull();
        assertThat(queue.poll()).isNull();
    }

    /**
     * 测试生产者线程在队列满时阻塞，并在 `wakeUpPuttingThread()` 调用后正确返回 false。
     */
    @Test
    public void testWakeUpPut() throws InterruptedException {
        FutureCompletingBlockingQueue<Integer> queue = new FutureCompletingBlockingQueue<>(1);

        CountDownLatch latch = new CountDownLatch(1);
        new Thread(() -> {
            try {
                assertThat(queue.put(0, 1234)).isTrue();
                // 第二次插入应阻塞，直到 wakeUpPuttingThread(0) 使其返回 false
                assertThat(queue.put(0, 5678)).isFalse();
                latch.countDown();
            } catch (InterruptedException e) {
                fail("线程意外中断");
            }
        }).start();

        // 等待短暂时间，让线程填满队列
        Thread.sleep(50);
        queue.wakeUpPuttingThread(0);
        latch.await();
        assertThat(latch.getCount()).isEqualTo(0);
    }

    /**
     * 测试并发环境下，多个生产者和多个消费者是否能正确运行。
     */
    @Test
    public void testConcurrency() throws InterruptedException {
        FutureCompletingBlockingQueue<Integer> queue = new FutureCompletingBlockingQueue<>(5);
        final int numValuesPerThread = 10000;
        final int numPuttingThreads = 5;
        List<Thread> threads = new ArrayList<>();

        // 5 个生产者线程
        for (int i = 0; i < numPuttingThreads; i++) {
            final int index = i;
            Thread t = new Thread(() -> {
                for (int j = 0; j < numValuesPerThread; j++) {
                    int base = index * numValuesPerThread;
                    try {
                        queue.put(index, base + j);
                    } catch (InterruptedException e) {
                        fail("生产者线程被中断");
                    }
                }
            });
            t.start();
            threads.add(t);
        }

        // 5 个消费者线程
        BitSet bitSet = new BitSet();
        AtomicInteger count = new AtomicInteger(0);
        for (int i = 0; i < 5; i++) {
            Thread t = new Thread(() -> {
                while (count.get() < numPuttingThreads * numValuesPerThread) {
                    Integer value = queue.poll();
                    if (value == null) {
                        continue;
                    }
                    count.incrementAndGet();
                    synchronized (bitSet) {
                        if (bitSet.get(value)) {
                            fail("值 " + value + " 被重复消费");
                        }
                        bitSet.set(value);
                    }
                }
            });
            t.start();
            threads.add(t);
        }
        for (Thread t : threads) {
            t.join();
        }
    }

    /**
     * 测试指定容量是否正确生效。
     */
    @Test
    public void testSpecifiedQueueCapacity() {
        final int capacity = 8000;
        final FutureCompletingBlockingQueue<Object> queue = new FutureCompletingBlockingQueue<>(capacity);
        assertThat(queue.remainingCapacity()).isEqualTo(capacity);
    }

    /**
     * 测试默认容量是否正确生效。
     */
    @Test
    public void testQueueDefaultCapacity() {
        final FutureCompletingBlockingQueue<Object> queue = new FutureCompletingBlockingQueue<>();
        assertThat(queue.remainingCapacity()).isEqualTo(DEFAULT_CAPACITY);
    }

    /**
     * 测试当队列为空时 `getAvailabilityFuture()` 是否未完成。
     */
    @Test
    public void testUnavailableWhenEmpty() {
        final FutureCompletingBlockingQueue<Object> queue = new FutureCompletingBlockingQueue<>();
        assertThat(queue.getAvailabilityFuture().isDone()).isFalse();
    }

    /**
     * 测试在 `put()` 之后 `getAvailabilityFuture()` 是否立即变为完成状态。
     */
    @Test
    public void testImmediatelyAvailableAfterPut() throws InterruptedException {
        final FutureCompletingBlockingQueue<Object> queue = new FutureCompletingBlockingQueue<>();
        queue.put(0, new Object());
        assertThat(queue.getAvailabilityFuture().isDone()).isTrue();
    }

    /**
     * 测试 `poll()` 后 `getAvailabilityFuture()` 是否重置为未完成状态。
     */
    @Test
    public void testUnavailableWhenBecomesEmpty() throws InterruptedException {
        final FutureCompletingBlockingQueue<Object> queue = new FutureCompletingBlockingQueue<>();
        queue.put(0, new Object());
        queue.poll();
        assertThat(queue.getAvailabilityFuture().isDone()).isFalse();
    }

    /**
     * 测试 `notifyAvailable()` 是否能正确使 `getAvailabilityFuture()` 完成。
     */
    @Test
    public void testAvailableAfterNotifyAvailable() {
        final FutureCompletingBlockingQueue<Object> queue = new FutureCompletingBlockingQueue<>();
        queue.notifyAvailable();
        assertThat(queue.getAvailabilityFuture().isDone()).isTrue();
    }

    /**
     * 测试 `poll()` 是否会重置 `getAvailabilityFuture()` 的完成状态。
     */
    @Test
    public void testPollResetsAvailability() {
        final FutureCompletingBlockingQueue<Object> queue = new FutureCompletingBlockingQueue<>();
        queue.notifyAvailable();

        final CompletableFuture<?> beforePoll = queue.getAvailabilityFuture();
        queue.poll();
        final CompletableFuture<?> afterPoll = queue.getAvailabilityFuture();

        assertThat(beforePoll.isDone()).isTrue();
        assertThat(afterPoll.isDone()).isFalse();
    }
}


