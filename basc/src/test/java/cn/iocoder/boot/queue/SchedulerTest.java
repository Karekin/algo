package cn.iocoder.boot.queue;

import cn.queue.Scheduler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class SchedulerTest {
    private Scheduler scheduler;

    // 在每个测试方法执行前创建一个新的 Scheduler 实例
    @BeforeEach
    public void setUp() {
        scheduler = new Scheduler();
    }

    // 在每个测试方法执行后关闭 Scheduler 实例，释放资源
    @AfterEach
    public void tearDown() {
        scheduler.shutdown();
    }

    // 测试单个任务的调度执行
    @Test
    public void testSingleTaskExecution() throws InterruptedException {
        // CountDownLatch 用于等待任务执行
        CountDownLatch latch = new CountDownLatch(1);

        // 调度一个延迟1秒的任务，任务执行时调用 latch.countDown() 以减少计数
        scheduler.schedule(() -> latch.countDown(), 1);

        // 等待任务执行，等待时间设置为2秒
        /*
            await 方法会阻塞当前线程，直到以下两种情况之一发生：
            1.	计数器 latch 的值减少到 0，表明任务已执行完成。
            2.	等待时间超过 2 秒，即达到超时。
            	    如果在 2 秒内任务成功执行并减少了计数（latch.countDown()），则 await 返回 true，表示任务按预期完成。
                	如果 2 秒内任务未执行完成（例如任务未运行或执行过慢），await 返回 false，表示超时。
         */
        assertTrue(latch.await(2, TimeUnit.SECONDS), "任务未在预期时间内执行");
    }

    // 测试多个任务的调度执行
    @Test
    public void testMultipleTasksExecution() throws InterruptedException {
        // 创建 CountDownLatch，计数设置为3，表示有3个任务需要执行
        CountDownLatch latch = new CountDownLatch(3);

        // 调度三个任务，分别延迟1秒、2秒、3秒后执行，每个任务都会调用 latch.countDown()
        scheduler.schedule(() -> latch.countDown(), 1);
        scheduler.schedule(() -> latch.countDown(), 2);
        scheduler.schedule(() -> latch.countDown(), 3);

        // 等待所有任务执行完成，超时时间设置为4秒
        assertTrue(latch.await(4, TimeUnit.SECONDS), "不是所有任务在预期时间内执行完成");
    }

    // 测试任务取消功能，确保被取消的任务不会被执行
    @Test
    public void testTaskCancellation() throws InterruptedException {
        // 创建 CountDownLatch，计数为1
        CountDownLatch latch = new CountDownLatch(1);

        // 调度一个延迟1秒的任务，并立即取消该任务
        long taskId = scheduler.schedule(() -> latch.countDown(), 1);
        scheduler.cancel(taskId);

        // 等待2秒，确保任务不会执行
        assertFalse(latch.await(2, TimeUnit.SECONDS), "已取消的任务不应被执行");
    }

    // 测试并发任务的调度执行
    @Test
    public void testConcurrentTaskScheduling() throws InterruptedException {
        int taskCount = 10;
        // 创建 CountDownLatch，计数设置为10，表示需要执行10个任务
        CountDownLatch latch = new CountDownLatch(taskCount);

        // 并发调度10个延迟1秒的任务，每个任务都会调用 latch.countDown() 以减少计数
        for (int i = 0; i < taskCount; i++) {
            scheduler.schedule(() -> latch.countDown(), 1);
        }

        // 等待所有任务执行完成，超时时间设置为2秒
        assertTrue(latch.await(2, TimeUnit.SECONDS), "并发任务未在预期时间内全部执行完成");
    }

    // 测试多个任务的执行顺序，验证任务的执行顺序是否符合预期
    @Test
    public void testTaskExecutionOrder() throws InterruptedException {
        // 创建 CountDownLatch，计数为3
        CountDownLatch latch = new CountDownLatch(3);
        StringBuilder result = new StringBuilder();

        // 调度三个任务，按延迟时间顺序执行
        scheduler.schedule(() -> {
            result.append("A");
            latch.countDown();
        }, 1);

        scheduler.schedule(() -> {
            result.append("B");
            latch.countDown();
        }, 2);

        scheduler.schedule(() -> {
            result.append("C");
            latch.countDown();
        }, 3);

        // 等待所有任务执行完成，超时时间设置为4秒
        assertTrue(latch.await(4, TimeUnit.SECONDS), "不是所有任务在预期时间内执行完成");

        // 验证任务的执行顺序是否符合预期（按调度顺序 A、B、C 执行）
        assertEquals("ABC", result.toString(), "任务未按预期顺序执行");
    }
}
