package cn.queue;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Scheduler {
    // 存储所有任务的Map，key为任务ID，value为任务对象
    private final Map<Long, Task> tasks = new ConcurrentHashMap<>();
    // 使用TreeMap将任务按执行时间进行排序，key为时间戳，value为任务ID列表
    private final TreeMap<Long, List<Long>> taskBuckets = new TreeMap<>();
    // 原子变量，用于生成唯一的任务ID
    private final AtomicLong idGen = new AtomicLong();
    // 可重入锁，用于线程安全的控制
    private final ReentrantLock lock = new ReentrantLock();
    // 条件变量，控制调度线程的等待和唤醒（Condition Variable）
    private final Condition runCv = lock.newCondition();
    // 标志是否停止调度器
    private volatile boolean stopped = false;
    // 线程池，用于执行任务
    private final ExecutorService executorService;

    public Scheduler() {
        // 初始化线程池为缓存线程池
        executorService = Executors.newCachedThreadPool();
        /*
            创建并启动调度线程：
            this::scheduleLoop 是方法引用的形式，其中 this 指向当前的 Scheduler 实例，而 scheduleLoop 是当前类中的一个方法。
            this::scheduleLoop 等价于 () -> this.scheduleLoop()，它代表一个不接受参数且没有返回值的 Runnable 接口的实例。
            方法引用 this::scheduleLoop 相当于创建了一个 Runnable 对象，并将 scheduleLoop 方法作为该对象的 run 方法实现。
         */
        Thread loopThread = new Thread(this::scheduleLoop); // 写法一

//        Thread loopThread = new Thread(() -> this.scheduleLoop()); // 写法二

//        Thread loopThread = new Thread(new Runnable() {  // 写法三
//            @Override
//            public void run() {
//                scheduleLoop();
//            }
//        });

        loopThread.setDaemon(true); // 设置为守护线程，后台运行
        loopThread.start();
    }

    // 停止调度器
    public void shutdown() {
        lock.lock();
        try {
            stopped = true; // 设置停止标志
            runCv.signalAll(); // 唤醒所有等待的线程
        } finally {
            lock.unlock();
        }
        executorService.shutdown(); // 关闭线程池
    }

    /*
        调度任务，并返回任务ID：
        schedule 方法的作用是将一个延迟执行的任务添加到调度器中，并返回该任务的唯一 ID。
        具体来说，它会创建一个包含任务逻辑和目标执行时间的任务对象，将该任务存储到任务列表中，并通知调度线程更新任务队列的状态。
     */
    public long schedule(Runnable task, long delayInSec) {
        lock.lock();
        try {
            long taskId = idGen.incrementAndGet(); // 生成唯一任务ID
            long targetTs = getCurrentMillis() + delayInSec * 1000; // 计算任务执行的目标时间戳

            // 创建任务对象并存储到tasks和taskBuckets中
            Task newTask = new Task(taskId, task, targetTs);
            tasks.put(taskId, newTask);
            taskBuckets.computeIfAbsent(targetTs, k -> new ArrayList<>()).add(taskId);
            runCv.signal(); // 唤醒调度线程检查新任务
            return taskId;
        } finally {
            lock.unlock();
        }
    }

    // 取消任务
    public void cancel(long taskId) {
        lock.lock();
        try {
            tasks.remove(taskId); // 从任务列表中移除任务
        } finally {
            lock.unlock();
        }
    }

    // 调度循环方法，在独立线程中运行
    private void scheduleLoop() {
        lock.lock();
        try {
            while (!stopped) { // 若未停止，持续执行循环
                if (taskBuckets.isEmpty()) { // 若没有任务，等待1秒
                    runCv.await(1, TimeUnit.SECONDS);
                    continue;
                }

                long nextTs = taskBuckets.firstKey(); // 获取最早的任务时间戳
                long delay = nextTs - getCurrentMillis(); // 计算当前时间与最早任务时间的延迟

                if (delay <= 0) { // 若延迟小于等于0，表示任务可以执行
                    List<Long> taskIds = taskBuckets.pollFirstEntry().getValue(); // 获取任务ID列表
                    for (long taskId : taskIds) {
                        Task task = tasks.remove(taskId); // 从任务列表中移除任务
                        if (task != null) {
                            executorService.submit(task.run); // 将任务提交到线程池中执行
                        }
                    }
                } else {
                    runCv.await(delay, TimeUnit.MILLISECONDS); // 等待指定延迟
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // 恢复中断状态
        } finally {
            lock.unlock();
        }
    }

    // 获取当前时间的毫秒数
    private long getCurrentMillis() {
        return Instant.now().toEpochMilli();
    }

    // 内部静态类，表示一个任务对象
    private static class Task {
        long taskId; // 任务ID
        Runnable run; // 任务执行的逻辑
        long targetTs; // 任务执行的目标时间戳

        Task(long taskId, Runnable run, long targetTs) {
            this.taskId = taskId;
            this.run = run;
            this.targetTs = targetTs;
        }
    }
}
