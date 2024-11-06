package cn.queue;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Scheduler {
    private final Map<Long, Task> tasks = new ConcurrentHashMap<>();
    private final TreeMap<Long, List<Long>> taskBuckets = new TreeMap<>();
    private final AtomicLong idGen = new AtomicLong();
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition runCv = lock.newCondition();
    private volatile boolean stopped = false;
    private final ExecutorService executorService;

    public Scheduler() {
        executorService = Executors.newCachedThreadPool();
        Thread loopThread = new Thread(this::scheduleLoop);
        loopThread.setDaemon(true); // 设置为守护线程，后台运行
        loopThread.start();
    }

    public void shutdown() {
        lock.lock();
        try {
            stopped = true;
            runCv.signalAll();
        } finally {
            lock.unlock();
        }
        executorService.shutdown();
    }

    public long schedule(Runnable task, long delayInSec) {
        lock.lock();
        try {
            long taskId = idGen.incrementAndGet();
            long targetTs = getCurrentMillis() + delayInSec * 1000;

            Task newTask = new Task(taskId, task, targetTs);
            tasks.put(taskId, newTask);
            taskBuckets.computeIfAbsent(targetTs, k -> new ArrayList<>()).add(taskId);
            runCv.signal(); // 唤醒调度线程检查新任务
            return taskId;
        } finally {
            lock.unlock();
        }
    }

    public void cancel(long taskId) {
        lock.lock();
        try {
            tasks.remove(taskId);
        } finally {
            lock.unlock();
        }
    }

    private void scheduleLoop() {
        lock.lock();
        try {
            while (!stopped) {
                if (taskBuckets.isEmpty()) {
                    runCv.await(1, TimeUnit.SECONDS);
                    continue;
                }

                long nextTs = taskBuckets.firstKey();
                long delay = nextTs - getCurrentMillis();

                if (delay <= 0) {
                    List<Long> taskIds = taskBuckets.pollFirstEntry().getValue();
                    for (long taskId : taskIds) {
                        Task task = tasks.remove(taskId);
                        if (task != null) {
                            executorService.submit(task.run);
                        }
                    }
                } else {
                    runCv.await(delay, TimeUnit.MILLISECONDS);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            lock.unlock();
        }
    }

    private long getCurrentMillis() {
        return Instant.now().toEpochMilli();
    }

    private static class Task {
        long taskId;
        Runnable run;
        long targetTs;

        Task(long taskId, Runnable run, long targetTs) {
            this.taskId = taskId;
            this.run = run;
            this.targetTs = targetTs;
        }
    }
}
