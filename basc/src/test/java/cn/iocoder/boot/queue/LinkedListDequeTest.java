package cn.iocoder.boot.queue;

import cn.queue.LinkedListDeque;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LinkedListDequeTest {
    private LinkedListDeque deque;

    // 每个测试方法执行前初始化一个新的 LinkedListDeque 实例
    @BeforeEach
    public void setUp() {
        deque = new LinkedListDeque();
    }

    // 测试队首入队和队尾入队操作
    @Test
    public void testPushFirstAndPushLast() {
        deque.pushFirst(1); // 队首入队元素 1
        deque.pushLast(2);  // 队尾入队元素 2
        deque.pushFirst(3); // 队首入队元素 3
        deque.pushLast(4);  // 队尾入队元素 4

        // 预期队列状态为：3, 1, 2, 4
        assertArrayEquals(new int[]{3, 1, 2, 4}, deque.toArray());
        assertEquals(4, deque.size(), "队列长度应为 4");
    }

    // 测试队首出队和队尾出队操作
    @Test
    public void testPopFirstAndPopLast() {
        deque.pushFirst(1);
        deque.pushLast(2);
        deque.pushFirst(3);
        deque.pushLast(4);

        // 队首出队，应返回 3，队列状态变为：1, 2, 4
        assertEquals(3, deque.popFirst(), "队首出队应返回 3");
        // 队尾出队，应返回 4，队列状态变为：1, 2
        assertEquals(4, deque.popLast(), "队尾出队应返回 4");

        // 预期队列状态为：1, 2
        assertArrayEquals(new int[]{1, 2}, deque.toArray());
        assertEquals(2, deque.size(), "队列长度应为 2");
    }

    // 测试查看队首和队尾元素操作
    @Test
    public void testPeekFirstAndPeekLast() {
        deque.pushFirst(1);
        deque.pushLast(2);

        // 查看队首元素，应为 1
        assertEquals(1, deque.peekFirst(), "队首元素应为 1");
        // 查看队尾元素，应为 2
        assertEquals(2, deque.peekLast(), "队尾元素应为 2");

        // 确保查看操作不会修改队列内容
        assertArrayEquals(new int[]{1, 2}, deque.toArray());
    }

    // 测试边界情况：空队列的各种操作
    @Test
    public void testEmptyDequeOperations() {
        // 空队列应判定为空
        assertTrue(deque.isEmpty(), "空队列应判定为空");

        // 空队列进行出队操作应抛出异常
        assertThrows(IndexOutOfBoundsException.class, deque::popFirst, "空队列队首出队应抛出异常");
        assertThrows(IndexOutOfBoundsException.class, deque::popLast, "空队列队尾出队应抛出异常");

        // 空队列进行查看操作应抛出异常
        assertThrows(IndexOutOfBoundsException.class, deque::peekFirst, "空队列查看队首元素应抛出异常");
        assertThrows(IndexOutOfBoundsException.class, deque::peekLast, "空队列查看队尾元素应抛出异常");
    }

    // 测试单元素队列的情况
    @Test
    public void testSingleElementDeque() {
        deque.pushFirst(1);

        // 队首和队尾均应为 1
        assertEquals(1, deque.peekFirst(), "队首元素应为 1");
        assertEquals(1, deque.peekLast(), "队尾元素应为 1");

        // 队首出队后，队列应为空
        assertEquals(1, deque.popFirst(), "队首出队应返回 1");
        assertTrue(deque.isEmpty(), "队列应为空");

        // 再次插入至队尾
        deque.pushLast(2);

        // 队首和队尾均应为 2
        assertEquals(2, deque.peekFirst(), "队首元素应为 2");
        assertEquals(2, deque.peekLast(), "队尾元素应为 2");

        // 队尾出队后，队列应为空
        assertEquals(2, deque.popLast(), "队尾出队应返回 2");
        assertTrue(deque.isEmpty(), "队列应为空");
    }

    // 测试转换为数组的方法
    @Test
    public void testToArray() {
        deque.pushFirst(1);
        deque.pushLast(2);
        deque.pushLast(3);

        // 预期队列状态为：1, 2, 3
        assertArrayEquals(new int[]{1, 2, 3}, deque.toArray(), "转换为数组应输出 [1, 2, 3]");
    }
}
