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
 * A custom implementation of a blocking queue in combination with a {@link CompletableFuture} to
 * hand over data between producer and consumer threads. Compared to a standard blocking queue, this
 * class offers an asynchronous availability notification mechanism via a {@link
 * #getAvailabilityFuture()}.
 *
 * <h3>Consumer Notifications</h3>
 *
 * <p>Instead of having the consumer block on {@link #take()} or periodically call {@link #poll()},
 * the consumer can obtain a {@link CompletableFuture} from {@link #getAvailabilityFuture()} that
 * completes whenever the queue becomes non-empty. This allows asynchronous callbacks or checks.
 *
 * <p>The future can also be explicitly completed by {@link #notifyAvailable()}, letting a consumer
 * know about a special condition without actually enqueueing an element.
 *
 * <p>Note: This class can produce <i>false positives</i> – i.e., the future may be completed even
 * if the queue has already been consumed by another thread. The consumer should always check
 * whether {@link #poll()} is actually returning an element, and if not, it should obtain a new
 * future to wait on.
 *
 * <h3>Producer Wakeup</h3>
 *
 * <p>Additionally, if a producer thread is blocked by a full queue, it can be gracefully woken up
 * via the {@link #wakeUpPuttingThread(int)} method without needing to interrupt the thread.
 *
 * @param <T> The type of elements stored in the queue.
 */
public class FutureCompletingBlockingQueue<T> {

    /**
     * A constant future that is always complete, indicating availability. If the queue is
     * guaranteed to be available, you can use this reference to avoid extra volatile writes.
     */
    public static final CompletableFuture<Void> AVAILABLE = CompletableFuture.completedFuture(null);

    // ------------------------------------------------------------------------
    //  Fields
    // ------------------------------------------------------------------------

    /** The maximum capacity of the queue. Must be > 0. */
    private final int capacity;

    /**
     * The availability future, acting like a "non-empty" indicator. If complete, it means the queue
     * might have data (but not guaranteed).
     */
    private CompletableFuture<Void> currentFuture;

    /** Lock used to protect all queue operations and conditions. */
    private final Lock lock;

    /** The internal queue storing elements. Access must hold {@link #lock}. */
    private final Queue<T> queue;

    /**
     * A queue of conditions for threads that are waiting to put elements when the queue is full.
     */
    private final Queue<Condition> notFull;

    /**
     * Holds a {@link ConditionAndFlag} for each producer thread, indexed by an integer
     * "threadIndex".
     */
    private ConditionAndFlag[] putConditionAndFlags;

    // ------------------------------------------------------------------------
    //  Constructors
    // ------------------------------------------------------------------------

    /** Creates a queue with a default capacity (e.g., 256). */
    public FutureCompletingBlockingQueue() {
        this(256);
    }

    /**
     * Creates a queue with the specified maximum capacity.
     *
     * @param capacity The maximum number of elements that can be stored in this queue.
     */
    public FutureCompletingBlockingQueue(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Queue capacity must be > 0");
        }
        this.capacity = capacity;
        this.queue = new ArrayDeque<>(capacity);
        this.lock = new ReentrantLock();
        this.putConditionAndFlags = new ConditionAndFlag[1];
        this.notFull = new ArrayDeque<>();

        // Initially, the queue is empty, so we use a non-completed future.
        this.currentFuture = new CompletableFuture<>();
    }

    // ------------------------------------------------------------------------
    //  Future / Notification logic
    // ------------------------------------------------------------------------

    /**
     * Returns a {@link CompletableFuture} that completes when the queue is (or may be) non-empty.
     * <p>
     * If the queue is empty, the returned future will complete the next time the queue
     * transitions to non-empty or if {@link #notifyAvailable()} is called. If multiple consumers
     * use this queue, the future may complete even if the data is taken by another consumer; after
     * a null {@link #poll()}, the consumer should call this method again to get a new future.
     *
     * @return A future completed when the queue is likely non-empty.
     */
    public CompletableFuture<Void> getAvailabilityFuture() {
        return currentFuture;
    }

    /**
     * Marks the current future as complete if not already complete, indicating potential
     * availability to consumers. Future calls to {@link #getAvailabilityFuture()} will return a
     * completed future, until the queue is discovered empty again by {@link #poll()} or {@link
     * #take()}.
     */
    public void notifyAvailable() {
        lock.lock();
        try {
            moveToAvailable();
        } finally {
            lock.unlock();
        }
    }

    // Internal method: sets currentFuture to AVAILABLE if not already.
    private void moveToAvailable() {
        if (currentFuture != AVAILABLE) {
            CompletableFuture<Void> old = currentFuture;
            currentFuture = AVAILABLE;
            old.complete(null);
        }
    }

    // Internal method: sets currentFuture to a new incomplete future if it was AVAILABLE.
    private void moveToUnAvailable() {
        if (currentFuture == AVAILABLE) {
            currentFuture = new CompletableFuture<>();
        }
    }

    // ------------------------------------------------------------------------
    //  Blocking Queue Logic
    // ------------------------------------------------------------------------

    /**
     * Enqueues the given element. If the queue is full, the calling thread will be blocked until
     * space is available or until it is woken up by {@link #wakeUpPuttingThread(int)}.
     *
     * @param threadIndex An integer identifying the calling producer thread.
     * @param element The element to enqueue (must not be null).
     * @return true if the element was successfully enqueued, false if the thread was woken up
     *     before being able to enqueue.
     * @throws InterruptedException if the thread is interrupted while waiting for space.
     */
    public boolean put(int threadIndex, T element) throws InterruptedException {
        if (element == null) {
            throw new NullPointerException("Element must not be null");
        }
        lock.lockInterruptibly();
        try {
            while (queue.size() >= capacity) {
                // If the wakeUp flag is set, return false immediately.
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
     * <b>Warning:</b> This is a blocking method that may repeatedly reset the availability future if
     * used heavily. It is intended primarily for testing or cases where blocking is acceptable.
     *
     * <p>Retrieves and removes the first element from the queue, blocking until one is available.
     * The method internally uses {@link #getAvailabilityFuture()} to avoid busy waiting, but
     * repeated calls may cause spurious resets of availability.
     *
     * @return The first element in the queue.
     * @throws InterruptedException If the thread is interrupted while waiting.
     */
    public T take() throws InterruptedException {
        T next;
        while ((next = poll()) == null) {
            // Wait for availability
            try {
                getAvailabilityFuture().get();
            } catch (ExecutionException | CompletionException e) {
                // This theoretically should not happen often
                throw new RuntimeException("Exception in queue future completion", e);
            }
        }
        return next;
    }

    /**
     * Retrieves and removes the first element of this queue, or returns null if this queue is
     * empty. If the queue becomes empty (or was already empty), availability is reset.
     *
     * @return The head of the queue, or null if empty.
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

    /**
     * If the producer thread with the given index is blocked in {@link #put(int, Object)} due to a
     * full queue, calling this method will let that thread exit with a <code>false</code> return
     * value instead of waiting indefinitely or being interrupted.
     *
     * @param threadIndex The identifier of the producer thread to wake up.
     */
    public void wakeUpPuttingThread(int threadIndex) {
        lock.lock();
        try {
            maybeCreateCondition(threadIndex);
            ConditionAndFlag caf = putConditionAndFlags[threadIndex];
            if (caf != null) {
                // Mark the thread to wake up
                caf.setWakeUp(true);
                // Signal the condition to release the producer from put()
                caf.condition().signal();
            }
        } finally {
            lock.unlock();
        }
    }

    // ------------------------------------------------------------------------
    //  Internal Helpers
    // ------------------------------------------------------------------------

    private void enqueue(T element) {
        final int sizeBefore = queue.size();
        queue.add(element);

        // If the queue was empty, mark availability
        if (sizeBefore == 0) {
            moveToAvailable();
        }
        // If there's space left and some thread is waiting to put, wake up one.
        if (sizeBefore < capacity - 1 && !notFull.isEmpty()) {
            signalNextPutter();
        }
    }

    private T dequeue() {
        final int sizeBefore = queue.size();
        final T element = queue.poll();

        // If the queue was full, we can signal another producer that space is now available
        if (sizeBefore == capacity && !notFull.isEmpty()) {
            signalNextPutter();
        }

        // If the queue is now empty, reset availability
        if (queue.isEmpty()) {
            moveToUnAvailable();
        }
        return element;
    }

    private void waitOnPut(int threadIndex) throws InterruptedException {
        maybeCreateCondition(threadIndex);
        Condition cond = putConditionAndFlags[threadIndex].condition();
        notFull.add(cond);
        cond.await();
    }

    private void signalNextPutter() {
        if (!notFull.isEmpty()) {
            notFull.poll().signal();
        }
    }

    private void maybeCreateCondition(int threadIndex) {
        if (putConditionAndFlags.length <= threadIndex) {
            putConditionAndFlags = Arrays.copyOf(putConditionAndFlags, threadIndex + 1);
        }
        if (putConditionAndFlags[threadIndex] == null) {
            putConditionAndFlags[threadIndex] = new ConditionAndFlag(lock.newCondition());
        }
    }

    private boolean getAndResetWakeUpFlag(int threadIndex) {
        maybeCreateCondition(threadIndex);
        ConditionAndFlag caf = putConditionAndFlags[threadIndex];
        if (caf.getWakeUp()) {
            caf.setWakeUp(false);
            return true;
        }
        return false;
    }

    // ------------------------------------------------------------------------
    //  Inner Class: ConditionAndFlag
    // ------------------------------------------------------------------------

    /**
     * A holder for a {@link Condition} and a boolean "wakeUp" flag. Each producer thread index gets
     * one of these for managing blocking on put() and graceful wake-up.
     */
    private static class ConditionAndFlag {
        private final Condition cond;
        private boolean wakeUp;

        ConditionAndFlag(Condition cond) {
            this.cond = cond;
            this.wakeUp = false;
        }

        Condition condition() {
            return cond;
        }

        boolean getWakeUp() {
            return wakeUp;
        }

        void setWakeUp(boolean value) {
            wakeUp = value;
        }
    }
}

