package cn.iocoder.boot.queue;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 一个自定义的阻塞队列实现，结合 {@link CompletableFuture}，用于生产者和消费者线程之间的数据交互。
 *
 * <h3>消费者通知机制</h3>
 *
 * <p>不同于标准的 {@link java.util.concurrent.BlockingQueue}，此队列支持**异步可用性通知**。
 * 消费者不必阻塞在 {@link #take()} 方法上或不断轮询 {@link #poll()} 方法，
 * 而是可以通过 {@link #getAvailabilityFuture()} 获取一个 {@link CompletableFuture}，
 * 该 Future 在队列有数据可用时会自动完成，从而支持**异步回调**。</p>
 *
 * <p>此外，调用 {@link #notifyAvailable()} 方法可手动触发 Future 完成，通知消费者有数据可用。
 * 但是，这并不保证队列内一定有数据，因此消费者需要调用 {@link #poll()} 进行实际获取。</p>
 *
 * <h3>生产者唤醒机制</h3>
 *
 * <p>如果生产者因队列已满而阻塞，它可以通过 {@link #wakeUpPuttingThread(int)} 被**安全地唤醒**，
 * 而无需使用中断（interrupt），从而避免异常中断影响正常逻辑。</p>
 *
 * @param <T> 队列中存储的元素类型
 */
public class FutureCompletingBlockingQueue<T> {

    /** 一个已经完成的 Future，表示队列是可用的（非空）。用于优化性能，避免不必要的 volatile 变量操作。 */
    public static final CompletableFuture<Void> AVAILABLE = CompletableFuture.completedFuture(null);

    // ------------------------------------------------------------------------
    //  内部字段
    // ------------------------------------------------------------------------

    /** 队列的最大容量，必须大于 0 */
    private final int capacity;

    /** 一个 Future，表示队列是否可能有数据（非严格保证）。如果完成，表示队列可能非空。 */
    private CompletableFuture<Void> currentFuture;

    /** 互斥锁，保护所有队列操作 */
    private final Lock lock;

    /** 存储队列元素的内部容器，需要获取锁后访问 */
    private final Queue<T> queue;

    /** 存储因队列已满而等待的生产者线程的条件变量 */
    private final Queue<Condition> notFull;

    /** 每个生产者线程的等待条件和唤醒标志，按线程索引存储 */
    private ConditionAndFlag[] putConditionAndFlags;

    // ------------------------------------------------------------------------
    //  构造方法
    // ------------------------------------------------------------------------

    /** 创建一个默认大小的队列（默认容量 256） */
    public FutureCompletingBlockingQueue() {
        this(256);
    }

    /**
     * 创建一个具有指定容量的队列。
     *
     * @param capacity 队列的最大容量，必须 > 0
     */
    public FutureCompletingBlockingQueue(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("队列容量必须大于 0");
        }
        this.capacity = capacity;
        this.queue = new ArrayDeque<>(capacity);
        this.lock = new ReentrantLock();
        this.putConditionAndFlags = new ConditionAndFlag[1];
        this.notFull = new ArrayDeque<>();

        // 初始状态：队列为空，因此使用未完成的 Future
        this.currentFuture = new CompletableFuture<>();
    }

    // ------------------------------------------------------------------------
    //  Future / 通知机制
    // ------------------------------------------------------------------------

    /**
     * 获取一个 Future，该 Future 在队列可能非空时会自动完成。
     *
     * <p>如果队列为空，返回的 Future 只有在队列变为非空时才会完成。
     * 但请注意，多个消费者可能竞争数据，因此 Future 完成并不严格保证队列仍有数据。</p>
     *
     * @return 当队列可能非空时完成的 Future
     */
    public CompletableFuture<Void> getAvailabilityFuture() {
        return currentFuture;
    }

    /**
     * 手动触发 Future 完成，表示队列可能有数据可用。
     *
     * <p>即使队列为空，也可以调用此方法，以便让消费者尝试获取数据。</p>
     */
    public void notifyAvailable() {
        lock.lock();
        try {
            moveToAvailable();
        } finally {
            lock.unlock();
        }
    }

    /** 内部方法：如果当前 Future 不是 AVAILABLE，则将其设为 AVAILABLE 并完成旧 Future */
    private void moveToAvailable() {
        if (currentFuture != AVAILABLE) {
            CompletableFuture<Void> old = currentFuture;
            currentFuture = AVAILABLE;
            old.complete(null);
        }
    }

    /** 内部方法：如果当前 Future 是 AVAILABLE，则创建一个新的未完成 Future */
    private void moveToUnAvailable() {
        if (currentFuture == AVAILABLE) {
            currentFuture = new CompletableFuture<>();
        }
    }

    // ------------------------------------------------------------------------
    //  阻塞队列逻辑
    // ------------------------------------------------------------------------

    /**
     * 添加元素到队列。如果队列已满，则当前线程会阻塞，直到有空间可用或被 {@link #wakeUpPuttingThread(int)} 唤醒。
     *
     * @param threadIndex 生产者线程的索引
     * @param element 需要加入队列的元素，不能为 null
     * @return 如果元素成功加入队列，则返回 true；如果线程被唤醒且未加入元素，则返回 false
     * @throws InterruptedException 如果线程在等待过程中被中断
     */
    public boolean put(int threadIndex, T element) throws InterruptedException {
        if (element == null) {
            throw new NullPointerException("队列元素不能为 null");
        }
        lock.lockInterruptibly();
        try {
            while (queue.size() >= capacity) {
                // 如果被唤醒，则返回 false
                if (getAndResetWakeUpFlag(threadIndex)) {
                    return false;
                }
                waitOnPut(threadIndex);
            }
            enqueue(element);
            return true;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 取出队列的第一个元素，并在队列为空时阻塞。
     *
     * @return 队列中的第一个元素
     * @throws InterruptedException 如果线程在等待过程中被中断
     */
    public T take() throws InterruptedException {
        T next;
        while ((next = poll()) == null) {
            // 使用 Future 避免忙等待
            try {
                getAvailabilityFuture().get();
            } catch (ExecutionException | CompletionException e) {
                throw new RuntimeException("队列 Future 发生异常", e);
            }
        }
        return next;
    }

    /**
     * 从队列中取出并移除第一个元素。如果队列为空，则返回 null。
     *
     * @return 队列中的第一个元素，如果队列为空，则返回 null
     */
    public T poll() {
        lock.lock();
        try {
            if (queue.isEmpty()) {
                moveToUnAvailable();
                return null;
            }
            return dequeue();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Retrieves, but does not remove, the first element of this queue, or returns null if it is
     * empty.
     *
     * @return The head of the queue, or null if empty.
     */
    public T peek() {
        lock.lock();
        try {
            return queue.peek();
        } finally {
            lock.unlock();
        }
    }

    /** @return The current number of elements in this queue. */
    public int size() {
        lock.lock();
        try {
            return queue.size();
        } finally {
            lock.unlock();
        }
    }

    /** @return True if this queue contains no elements, false otherwise. */
    public boolean isEmpty() {
        lock.lock();
        try {
            return queue.isEmpty();
        } finally {
            lock.unlock();
        }
    }

    /**
     * @return The remaining capacity of this queue (capacity - currentSize).
     */
    public int remainingCapacity() {
        lock.lock();
        try {
            return capacity - queue.size();
        } finally {
            lock.unlock();
        }
    }

    // ------------------------------------------------------------------------
    //  生产者唤醒机制
    // ------------------------------------------------------------------------

    /**
     * 唤醒正在等待的生产者线程，使其可以继续执行（例如因为队列已满而阻塞的线程）。
     *
     * @param threadIndex 生产者线程的索引
     */
    public void wakeUpPuttingThread(int threadIndex) {
        lock.lock();
        try {
            maybeCreateCondition(threadIndex);
            ConditionAndFlag caf = putConditionAndFlags[threadIndex];
            if (caf != null) {
                caf.setWakeUp(true);
                caf.condition().signal();
            }
        } finally {
            lock.unlock();
        }
    }

    // ------------------------------------------------------------------------
//  内部辅助方法（Internal Helpers）
// ------------------------------------------------------------------------

    /**
     * 将元素添加到队列。如果队列为空，则同时更新可用性状态，并尝试唤醒等待的生产者。
     *
     * @param element 需要入队的元素
     */
    private void enqueue(T element) {
        final int sizeBefore = queue.size();
        queue.add(element);

        // 如果队列之前是空的，说明新的数据到来了，需要标记队列为“可用”
        if (sizeBefore == 0) {
            moveToAvailable();
        }

        // 如果队列未满，并且有生产者在等待空位，则唤醒一个等待中的生产者
        if (sizeBefore < capacity - 1 && !notFull.isEmpty()) {
            signalNextPutter();
        }
    }

    /**
     * 从队列中取出一个元素。如果队列为空，则重置可用性状态，并尝试唤醒等待的生产者。
     *
     * @return 队列中的第一个元素，如果队列为空，则返回 null
     */
    private T dequeue() {
        final int sizeBefore = queue.size();
        final T element = queue.poll();

        // 如果队列之前是满的，现在有了空位，可以唤醒一个等待的生产者
        if (sizeBefore == capacity && !notFull.isEmpty()) {
            signalNextPutter();
        }

        // 如果队列现在变为空了，需要标记队列为“不可用”
        if (queue.isEmpty()) {
            moveToUnAvailable();
        }
        return element;
    }

    /**
     * 让当前生产者线程进入等待状态，直到有空位可以插入数据。
     *
     * @param threadIndex 当前生产者线程的索引
     * @throws InterruptedException 如果线程在等待过程中被中断
     */
    private void waitOnPut(int threadIndex) throws InterruptedException {
        // 确保当前线程有对应的 Condition 变量
        maybeCreateCondition(threadIndex);

        // 获取当前线程的 Condition 变量，并加入等待队列
        Condition cond = putConditionAndFlags[threadIndex].condition();
        notFull.add(cond);

        // 线程进入等待状态，直到被唤醒（通常是因为队列中出现了空位）
        cond.await();
    }

    /**
     * 唤醒下一个等待的生产者线程，允许它继续插入数据。
     */
    private void signalNextPutter() {
        if (!notFull.isEmpty()) {
            notFull.poll().signal();
        }
    }

    /**
     * 确保给定的生产者线程索引有一个可用的 Condition 变量（用于等待和唤醒）。
     *
     * @param threadIndex 生产者线程的索引
     */
    private void maybeCreateCondition(int threadIndex) {
        // 如果当前线程索引超出了数组范围，扩展数组
        if (putConditionAndFlags.length <= threadIndex) {
            putConditionAndFlags = Arrays.copyOf(putConditionAndFlags, threadIndex + 1);
        }

        // 如果该索引对应的 ConditionAndFlag 为空，则创建新的 Condition 变量
        if (putConditionAndFlags[threadIndex] == null) {
            putConditionAndFlags[threadIndex] = new ConditionAndFlag(lock.newCondition());
        }
    }

    /**
     * 检查并重置当前线程的“唤醒标志”。如果标志为 true，则重置它并返回 true。
     *
     * @param threadIndex 生产者线程的索引
     * @return 如果线程需要被唤醒，则返回 true；否则返回 false
     */
    private boolean getAndResetWakeUpFlag(int threadIndex) {
        maybeCreateCondition(threadIndex);
        ConditionAndFlag caf = putConditionAndFlags[threadIndex];

        // 如果当前线程被标记为需要唤醒，则重置标志并返回 true
        if (caf.getWakeUp()) {
            caf.setWakeUp(false);
            return true;
        }
        return false;
    }

// ------------------------------------------------------------------------
//  内部类：ConditionAndFlag
// ------------------------------------------------------------------------

    /**
     * 维护一个 {@link Condition} 变量和一个 boolean 类型的“唤醒标志”。
     * 这个类用于管理生产者线程的阻塞与唤醒状态。
     */
    private static class ConditionAndFlag {
        private final Condition cond;  // 线程等待的条件变量
        private boolean wakeUp;        // 是否需要唤醒该线程的标志

        /**
         * 构造方法，初始化 Condition 变量。
         *
         * @param cond 线程等待的条件变量
         */
        ConditionAndFlag(Condition cond) {
            this.cond = cond;
            this.wakeUp = false;
        }

        /**
         * 获取 Condition 变量。
         *
         * @return 线程等待的条件变量
         */
        Condition condition() {
            return cond;
        }

        /**
         * 获取“唤醒标志”。
         *
         * @return 如果线程应被唤醒，则返回 true；否则返回 false
         */
        boolean getWakeUp() {
            return wakeUp;
        }

        /**
         * 设置“唤醒标志”。
         *
         * @param value 是否应唤醒该线程
         */
        void setWakeUp(boolean value) {
            wakeUp = value;
        }
    }

}

