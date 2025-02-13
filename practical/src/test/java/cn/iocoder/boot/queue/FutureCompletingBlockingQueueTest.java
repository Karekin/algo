package cn.iocoder.boot.queue;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 */


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
 * Unit tests for the {@link FutureCompletingBlockingQueue} class,
 * verifying its behavior in various scenarios (capacity constraints,
 * concurrency, etc.).
 */
public class FutureCompletingBlockingQueueTest {

    private static final int DEFAULT_CAPACITY = 256; // match the default from the no-arg constructor

    @Test
    public void testBasics() throws InterruptedException {
        FutureCompletingBlockingQueue<Integer> queue = new FutureCompletingBlockingQueue<>(5);

        CompletableFuture<Void> future = queue.getAvailabilityFuture();
        assertThat(queue.isEmpty()).isTrue();
        assertThat(queue.size()).isEqualTo(0);

        queue.put(0, 1234);

        // after putting an element, the previously obtained future should be done
        assertThat(future.isDone()).isTrue();
        assertThat(queue.size()).isEqualTo(1);
        assertThat(queue.isEmpty()).isFalse();
        assertThat(queue.remainingCapacity()).isEqualTo(4);
        assertThat(queue.peek()).isNotNull();
        assertThat((int) queue.peek()).isEqualTo(1234);

        int polled = queue.poll();
        assertThat(polled).isEqualTo(1234);

        assertThat(queue.size()).isEqualTo(0);
        assertThat(queue.isEmpty()).isTrue();
        assertThat(queue.remainingCapacity()).isEqualTo(5);
    }

    @Test
    public void testPoll() throws InterruptedException {
        FutureCompletingBlockingQueue<Integer> queue = new FutureCompletingBlockingQueue<>();
        queue.put(0, 1234);
        Integer value = queue.poll();
        assertThat(value).isNotNull();
        assertThat(value).isEqualTo(1234);
    }

    @Test
    public void testPollEmptyQueue() throws InterruptedException {
        FutureCompletingBlockingQueue<Integer> queue = new FutureCompletingBlockingQueue<>();
        queue.put(0, 1234);

        assertThat(queue.poll()).isNotNull();
        assertThat(queue.poll()).isNull();
        assertThat(queue.poll()).isNull();
    }

    @Test
    public void testWakeUpPut() throws InterruptedException {
        FutureCompletingBlockingQueue<Integer> queue = new FutureCompletingBlockingQueue<>(1);

        CountDownLatch latch = new CountDownLatch(1);
        new Thread(() -> {
            try {
                assertThat(queue.put(0, 1234)).isTrue();
                // second put will block, but we do wakeUpPuttingThread(0) => should return false
                assertThat(queue.put(0, 5678)).isFalse();
                latch.countDown();
            } catch (InterruptedException e) {
                fail("Interrupted unexpectedly.");
            }
        }).start();

        // let the first element fill the queue, then wake up the thread
        Thread.sleep(50);
        queue.wakeUpPuttingThread(0);
        latch.await();
        assertThat(latch.getCount()).isEqualTo(0);
    }

    @Test
    public void testConcurrency() throws InterruptedException {
        FutureCompletingBlockingQueue<Integer> queue = new FutureCompletingBlockingQueue<>(5);
        final int numValuesPerThread = 10000;
        final int numPuttingThreads = 5;
        List<Thread> threads = new ArrayList<>();

        // 5 threads putting data
        for (int i = 0; i < numPuttingThreads; i++) {
            final int index = i;
            Thread t = new Thread(() -> {
                for (int j = 0; j < numValuesPerThread; j++) {
                    int base = index * numValuesPerThread;
                    try {
                        queue.put(index, base + j);
                    } catch (InterruptedException e) {
                        fail("Putting thread interrupted.");
                    }
                }
            });
            t.start();
            threads.add(t);
        }

        // 5 threads consuming data
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
                            fail("Value " + value + " has been consumed before");
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

    @Test
    public void testSpecifiedQueueCapacity() {
        final int capacity = 8000;
        final FutureCompletingBlockingQueue<Object> queue = new FutureCompletingBlockingQueue<>(capacity);
        assertThat(queue.remainingCapacity()).isEqualTo(capacity);
    }

    @Test
    public void testQueueDefaultCapacity() {
        final FutureCompletingBlockingQueue<Object> queue = new FutureCompletingBlockingQueue<>();
        assertThat(queue.remainingCapacity()).isEqualTo(DEFAULT_CAPACITY);
    }

    @Test
    public void testUnavailableWhenEmpty() {
        final FutureCompletingBlockingQueue<Object> queue = new FutureCompletingBlockingQueue<>();
        assertThat(queue.getAvailabilityFuture().isDone()).isFalse();
    }

    @Test
    public void testImmediatelyAvailableAfterPut() throws InterruptedException {
        final FutureCompletingBlockingQueue<Object> queue = new FutureCompletingBlockingQueue<>();
        queue.put(0, new Object());
        assertThat(queue.getAvailabilityFuture().isDone()).isTrue();
    }

    @Test
    public void testFutureBecomesAvailableAfterPut() throws InterruptedException {
        final FutureCompletingBlockingQueue<Object> queue = new FutureCompletingBlockingQueue<>();
        final CompletableFuture<?> future = queue.getAvailabilityFuture();
        queue.put(0, new Object());
        assertThat(future.isDone()).isTrue();
    }

    @Test
    public void testUnavailableWhenBecomesEmpty() throws InterruptedException {
        final FutureCompletingBlockingQueue<Object> queue = new FutureCompletingBlockingQueue<>();
        queue.put(0, new Object());
        queue.poll();
        // after poll => queue empty => new future should not be done
        assertThat(queue.getAvailabilityFuture().isDone()).isFalse();
    }

    @Test
    public void testAvailableAfterNotifyAvailable() {
        final FutureCompletingBlockingQueue<Object> queue = new FutureCompletingBlockingQueue<>();
        queue.notifyAvailable();
        assertThat(queue.getAvailabilityFuture().isDone()).isTrue();
    }

    @Test
    public void testFutureBecomesAvailableAfterNotifyAvailable() {
        final FutureCompletingBlockingQueue<Object> queue = new FutureCompletingBlockingQueue<>();
        final CompletableFuture<?> future = queue.getAvailabilityFuture();
        queue.notifyAvailable();
        assertThat(future.isDone()).isTrue();
    }

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

