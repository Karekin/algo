package cn.iocoder.boot.heap;


import cn.heap.FixedMaxHeap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FixedMaxHeapTest {

    private FixedMaxHeap heap;

    @BeforeEach
    void setUp() {
        heap = new FixedMaxHeap(5);  // 初始化一个容量为5的堆
    }

    @Test
    void testPushAndPeek() {
        heap.push(10);
        assertEquals(10, heap.peek());

        heap.push(20);
        assertEquals(20, heap.peek());

        heap.push(5);
        assertEquals(20, heap.peek());  // 检查堆顶是否保持最大值
    }

    @Test
    void testPop() {
        heap.push(10);
        heap.push(20);
        heap.push(5);

        assertEquals(20, heap.pop());  // 检查最大值是否弹出
        assertEquals(10, heap.pop());
        assertEquals(5, heap.pop());
    }

    @Test
    void testHeapSize() {
        assertEquals(0, heap.size());

        heap.push(10);
        heap.push(20);

        assertEquals(2, heap.size());

        heap.pop();
        assertEquals(1, heap.size());
    }

    @Test
    void testIsEmpty() {
        assertTrue(heap.isEmpty());

        heap.push(10);
        assertFalse(heap.isEmpty());

        heap.pop();
        assertTrue(heap.isEmpty());
    }

    @Test
    void testHeapCapacityExceeded() {
        heap.push(10);
        heap.push(20);
        heap.push(30);
        heap.push(40);
        heap.push(50);

        assertThrows(IllegalStateException.class, () -> heap.push(60));  // 超过容量后，抛出异常
    }

    @Test
    void testPeekOnEmptyHeap() {
        assertThrows(IndexOutOfBoundsException.class, () -> heap.peek());
    }

    @Test
    void testPopOnEmptyHeap() {
        assertThrows(IndexOutOfBoundsException.class, () -> heap.pop());
    }

    @Test
    void testHeapConstructionFromList() {
        List<Integer> inputList = Arrays.asList(3, 5, 1, 10, 2);
        FixedMaxHeap heap = new FixedMaxHeap(inputList, 5);

        // 检查构建的堆的大小是否正确
        assertEquals(5, heap.size());

        // 检查堆顶元素是否为列表中的最大值（10）
        assertEquals(10, heap.peek());

        // 按顺序移除元素并验证是否为从大到小的顺序
        assertEquals(10, heap.pop());
        assertEquals(5, heap.pop());
        assertEquals(3, heap.pop());
        assertEquals(2, heap.pop());
        assertEquals(1, heap.pop());

        // 检查堆在完全移除后是否为空
        assertTrue(heap.isEmpty());
    }
}
