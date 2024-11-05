package cn.iocoder.boot.queue;


import cn.queue.BlockingQueue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;

class BlockingQueueTest {
    private BlockingQueue<Integer> queue;

    @BeforeEach
    void setUp() {
        queue = new BlockingQueue<>(3); // 设置队列容量为3
    }

    @Test
    void testPushAndSize() throws InterruptedException {
        queue.push(1);
        queue.push(2);
        queue.push(3);
        assertEquals(3, queue.size(), "队列大小应为3");
    }

    @Test
    void testPop() throws InterruptedException {
        queue.push(1);
        queue.push(2);
        assertEquals(1, queue.pop(), "出队的第一个元素应为1");
        assertEquals(2, queue.pop(), "出队的第二个元素应为2");
        assertEquals(0, queue.size(), "出队后队列应为空");
    }

    @Test
    void testBlockingOnFullQueue() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        queue.push(1);
        queue.push(2);
        queue.push(3);

        // 另一个线程尝试入队，它将等待直到有空间
        executor.submit(() -> {
            try {
                queue.push(4);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        TimeUnit.SECONDS.sleep(1); // 给其他线程时间以确认它是否被阻塞
        assertEquals(3, queue.size(), "队列满时，大小应保持为3");

        // 出队一个元素，允许等待的线程继续入队
        queue.pop();
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.SECONDS);

        assertEquals(3, queue.size(), "出队一个元素后，队列大小应回到3");
    }

    @Test
    void testBlockingOnEmptyQueue() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(2);

        // 一个线程尝试从空队列中出队，它将等待直到有元素
        executor.submit(() -> {
            try {
                queue.pop();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        TimeUnit.SECONDS.sleep(1); // 给其他线程时间以确认它是否被阻塞
        assertEquals(0, queue.size(), "队列为空时，大小应保持为0");

        // 入队一个元素，允许等待的线程继续出队
        queue.push(1);
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.SECONDS);

        assertEquals(0, queue.size(), "出队后队列应为空");
    }

    @Test
    void testConcurrentPushAndPop() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(4);

        // 两个线程执行入队操作
        executor.submit(() -> {
            try {
                queue.push(1);
                queue.push(2);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        executor.submit(() -> {
            try {
                queue.push(3);
                queue.push(4);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // 两个线程执行出队操作
        executor.submit(() -> {
            try {
                queue.pop();
                queue.pop();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        executor.submit(() -> {
            try {
                queue.pop();
                queue.pop();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.SECONDS);

        assertTrue(queue.size() == 0, "并发入队和出队后队列应为空");
    }
}
