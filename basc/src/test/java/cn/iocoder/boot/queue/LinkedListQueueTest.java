package cn.iocoder.boot.queue;


import cn.queue.LinkedListQueue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LinkedListQueueTest {
    private LinkedListQueue queue;

    @BeforeEach
    void setUp() {
        queue = new LinkedListQueue();
    }

    @Test
    void testIsEmptyOnNewQueue() {
        assertTrue(queue.isEmpty(), "新队列应为空");
    }

    @Test
    void testPushAndSize() {
        queue.push(10);
        queue.push(20);
        queue.push(30);
        assertEquals(3, queue.size(), "入队3个元素后队列大小应为3");
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
    }
}

