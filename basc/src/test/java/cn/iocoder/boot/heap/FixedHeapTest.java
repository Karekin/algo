package cn.iocoder.boot.heap;

import cn.heap.FixedHeap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FixedHeapTest {

    private FixedHeap minHeap;
    private FixedHeap maxHeap;

    @BeforeEach
    void setUp() {
        minHeap = new FixedHeap(5, Comparator.naturalOrder()); // 小顶堆
        maxHeap = new FixedHeap(5, Comparator.reverseOrder()); // 大顶堆
    }

    @Test
    void testMinHeapConstructionFromList() {
        List<Integer> inputList = Arrays.asList(10, 3, 5, 20, 7);
        FixedHeap heapFromList = new FixedHeap(inputList, 5, Comparator.naturalOrder());

        // 检查堆的大小是否正确
        assertEquals(5, heapFromList.size());

        // 检查堆顶元素是否为列表中的最小值
        assertEquals(3, heapFromList.peek());
    }

    @Test
    void testMaxHeapConstructionFromList() {
        List<Integer> inputList = Arrays.asList(10, 3, 5, 20, 7);
        FixedHeap heapFromList = new FixedHeap(inputList, 5, Comparator.reverseOrder());

        // 检查堆的大小是否正确
        assertEquals(5, heapFromList.size());

        // 检查堆顶元素是否为列表中的最大值
        assertEquals(20, heapFromList.peek());
    }

    @Test
    void testMinHeapPushAndPeek() {
        minHeap.push(10);
        assertEquals(10, minHeap.peek());

        minHeap.push(5);
        assertEquals(5, minHeap.peek());

        minHeap.push(15);
        assertEquals(5, minHeap.peek()); // 检查堆顶是否保持最小值
    }

    @Test
    void testMaxHeapPushAndPeek() {
        maxHeap.push(10);
        assertEquals(10, maxHeap.peek());

        maxHeap.push(20);
        assertEquals(20, maxHeap.peek());

        maxHeap.push(5);
        assertEquals(20, maxHeap.peek()); // 检查堆顶是否保持最大值
    }

    @Test
    void testMinHeapPop() {
        minHeap.push(10);
        minHeap.push(3);
        minHeap.push(5);

        assertEquals(3, minHeap.pop());  // 检查最小值是否弹出
        assertEquals(5, minHeap.pop());
        assertEquals(10, minHeap.pop());
    }

    @Test
    void testMaxHeapPop() {
        maxHeap.push(10);
        maxHeap.push(3);
        maxHeap.push(15);

        assertEquals(15, maxHeap.pop());  // 检查最大值是否弹出
        assertEquals(10, maxHeap.pop());
        assertEquals(3, maxHeap.pop());
    }

    @Test
    void testHeapSize() {
        assertEquals(0, minHeap.size());

        minHeap.push(10);
        minHeap.push(20);

        assertEquals(2, minHeap.size());

        minHeap.pop();
        assertEquals(1, minHeap.size());
    }

    @Test
    void testIsEmpty() {
        assertTrue(minHeap.isEmpty());

        minHeap.push(10);
        assertFalse(minHeap.isEmpty());

        minHeap.pop();
        assertTrue(minHeap.isEmpty());
    }

    @Test
    void testHeapCapacityExceeded() {
        minHeap.push(10);
        minHeap.push(20);
        minHeap.push(30);
        minHeap.push(40);
        minHeap.push(50);

        // 插入第6个元素时应抛出异常
        assertThrows(IllegalStateException.class, () -> minHeap.push(60));
    }

    @Test
    void testPeekOnEmptyHeap() {
        assertThrows(IndexOutOfBoundsException.class, () -> minHeap.peek());
    }

    @Test
    void testPopOnEmptyHeap() {
        assertThrows(IndexOutOfBoundsException.class, () -> minHeap.pop());
    }
}

