package cn.iocoder.boot.heap;

import cn.heap.FixedMinHeap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FixedMinHeapTest {

    private FixedMinHeap heap;

    @BeforeEach
    void setUp() {
        heap = new FixedMinHeap(5); // 初始化一个容量为5的堆
    }

    @Test
    void testHeapConstructionFromList() {
        List<Integer> inputList = Arrays.asList(10, 3, 5, 20, 7);
        FixedMinHeap heapFromList = new FixedMinHeap(inputList, 5);

        // 检查堆的大小是否正确
        assertEquals(5, heapFromList.size());

        // 检查堆顶元素是否为列表中的最小值
        assertEquals(3, heapFromList.peek());
    }

    @Test
    void testPushAndPeek() {
        heap.push(10);
        assertEquals(10, heap.peek());

        heap.push(5);
        assertEquals(5, heap.peek());

        heap.push(15);
        assertEquals(5, heap.peek()); // 检查堆顶是否保持最小值
    }

    @Test
    void testPop() {
        heap.push(10);
        heap.push(3);
        heap.push(5);

        assertEquals(3, heap.pop());  // 检查最小值是否弹出
        assertEquals(5, heap.pop());
        assertEquals(10, heap.pop());
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

        // 插入第6个元素时应抛出异常
        assertThrows(IllegalStateException.class, () -> heap.push(60));
    }

    @Test
    void testPeekOnEmptyHeap() {
        assertThrows(IndexOutOfBoundsException.class, () -> heap.peek());
    }

    @Test
    void testPopOnEmptyHeap() {
        assertThrows(IndexOutOfBoundsException.class, () -> heap.pop());
    }
}
