package cn.iocoder.boot.queue;

import cn.queue.ArrayQueue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ArrayQueueTest {
    private ArrayQueue queue;

    @BeforeEach
    void setUp() {
        queue = new ArrayQueue(3); // 设置队列容量为3，用于测试边界情况
    }

    @Test
    void testIsEmptyOnNewQueue() {
        assertTrue(queue.isEmpty(), "新队列应为空");
    }

    @Test
    void testCapacity() {
        assertEquals(3, queue.capacity(), "队列容量应为3");
    }

    @Test
    void testPushAndSize() {
        queue.push(10);
        queue.push(20);
        queue.push(30);
        assertEquals(3, queue.size(), "入队3个元素后队列大小应为3");
    }

    @Test
    void testPushOnFullQueue() {
        queue.push(10);
        queue.push(20);
        queue.push(30);
        queue.push(40); // 队列已满，推入此元素应无效
        assertEquals(3, queue.size(), "队列满时不能再入队，应保持队列大小为3");
    }

    @Test
    void testPeek() {
        queue.push(10);
        queue.push(20);
        assertEquals(10, queue.peek(), "队首元素应为10");
    }

    @Test
    void testPop() {
        queue.push(10);
        queue.push(20);
        queue.push(30);

        assertEquals(10, queue.pop(), "第一次出队应返回10");
        assertEquals(20, queue.pop(), "第二次出队应返回20");
        assertEquals(30, queue.pop(), "第三次出队应返回30");
        assertTrue(queue.isEmpty(), "出队后队列应为空");
    }

    @Test
    void testPopOnEmptyQueue() {
        Exception exception = assertThrows(IndexOutOfBoundsException.class, queue::pop);
        assertNull(exception.getMessage(), "空队列出队应抛出异常");
    }

    @Test
    void testPeekOnEmptyQueue() {
        Exception exception = assertThrows(IndexOutOfBoundsException.class, queue::peek);
        assertNull(exception.getMessage(), "空队列取队首应抛出异常");
    }

    @Test
    void testToArray() {
        queue.push(40);
        queue.push(50);
        int[] expectedArray = {40, 50};
        assertArrayEquals(expectedArray, queue.toArray(), "toArray 方法返回值应匹配队列元素顺序");

        queue.push(60);
        queue.pop(); // 出队第一个元素40，队列内剩下50和60
        int[] expectedArrayAfterPop = {50, 60};
        assertArrayEquals(expectedArrayAfterPop, queue.toArray(), "出队后 toArray 返回值应匹配队列当前元素顺序");
    }

    @Test
    void testCircularBehavior() {
        queue.push(10);
        queue.push(20);
        queue.push(30);

        queue.pop(); // 出队第一个元素，队列为 {20, 30}
        queue.push(40); // 入队新元素，测试环形行为

        int[] expectedArray = {20, 30, 40};
        assertArrayEquals(expectedArray, queue.toArray(), "在环形队列中入队新元素后 toArray 应正确反映队列内容");
    }
}
