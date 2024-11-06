package cn.iocoder.boot.stack;

import cn.stack.LockFreeStack;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class LockFreeStackTest {
    private LockFreeStack<Integer> stack;

    @BeforeEach
    public void setUp() {
        stack = new LockFreeStack<>();
    }

    @Test
    public void testPushAndPop() {
        stack.push(1);
        stack.push(2);
        stack.push(3);

        assertEquals(3, stack.pop());
        assertEquals(2, stack.pop());
        assertEquals(1, stack.pop());
    }

    @Test
    public void testEmpty() {
        assertTrue(stack.empty());

        stack.push(1);
        assertFalse(stack.empty());

        stack.pop();
        assertTrue(stack.empty());
    }

    @Test
    public void testPopOnEmptyStack() {
        assertThrows(IllegalStateException.class, stack::pop, "Stack is empty");
    }

    @Test
    public void testConcurrentPushAndPop() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(10);

        // 5个线程并发执行 push 操作
        for (int i = 0; i < 5; i++) {
            final int value = i;
            executorService.submit(() -> stack.push(value));
        }

        // 5个线程并发执行 pop 操作
        for (int i = 0; i < 5; i++) {
            executorService.submit(() -> {
                try {
                    stack.pop();
                } catch (IllegalStateException e) {
                    // 忽略栈空异常
                }
            });
        }

        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.SECONDS);

        // 检查栈是否为空（所有元素已被pop）
        assertTrue(stack.empty());
    }

    @Test
    public void testToString() {
        stack.push(1);
        stack.push(2);
        stack.push(3);

        String stackString = stack.toString();
        assertEquals("3->2->1->null", stackString);
    }
}
