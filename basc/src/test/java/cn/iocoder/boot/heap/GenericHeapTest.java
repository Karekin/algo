package cn.iocoder.boot.heap;

import cn.heap.GenericHeap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.*;

public class GenericHeapTest {
    private GenericHeap<Integer> minHeap;
    private GenericHeap<Integer> maxHeap;

    @BeforeEach
    public void setUp() {
        minHeap = new GenericHeap<>(5, Comparator.naturalOrder()); // 最小堆
        maxHeap = new GenericHeap<>(5, Comparator.reverseOrder()); // 最大堆
    }

    @Test
    public void testPushAndPeek() {
        minHeap.push(10);
        minHeap.push(5);
        minHeap.push(20);

        assertEquals(5, minHeap.peek()); // 最小堆，堆顶是最小值
    }

    @Test
    public void testPop() {
        minHeap.push(10);
        minHeap.push(5);
        minHeap.push(20);

        assertEquals(5, minHeap.pop()); // 最小堆，堆顶是最小值
        assertEquals(10, minHeap.pop()); // 移除后堆顶更新
        assertEquals(20, minHeap.pop());
        assertTrue(minHeap.isEmpty()); // 所有元素移除后堆为空
    }

    @Test
    public void testHeapCapacityExceeded() {
        minHeap.push(10);
        minHeap.push(5);
        minHeap.push(20);
        minHeap.push(15);
        minHeap.push(1);

        assertThrows(IllegalStateException.class, () -> minHeap.push(25), "Heap capacity exceeded.");
    }

    @Test
    public void testOfferWithinCapacity() {
        minHeap.offer(10);
        minHeap.offer(5);
        minHeap.offer(20);
        minHeap.offer(15);
        minHeap.offer(1);

        // 5个元素后，堆容量已满
        assertEquals(5, minHeap.size());
    }

    /**
     * 小顶堆的用途：此时，小顶堆用于维护前 k 大的元素。当堆已满时，只有当新元素比堆顶大（优先级更高）时，才替换堆顶。
     */
    @Test
    public void testOfferExceedCapacityWithHigherPriority() {
        minHeap.offer(10);
        minHeap.offer(5);
        minHeap.offer(20);
        minHeap.offer(15);
        minHeap.offer(1);

        // 堆已满，尝试插入优先级更低的元素
        minHeap.offer(0); // 0 的优先级低于堆顶 1
        assertEquals(1, minHeap.peek()); // 堆顶应保持为 1
        assertEquals(5, minHeap.size());

        // 尝试插入一个优先级更高的元素
        minHeap.offer(30); // 30 的优先级比堆顶 1 高
        assertEquals(5, minHeap.size());
        assertEquals(5, minHeap.peek()); // 新的堆顶应该是堆中的最小元素 5
    }


    /**
     * 大顶堆的用途：此时，大顶堆用于维护前 k 小的元素。当堆已满时，只有当新元素比堆顶小（优先级更高）时，才替换堆顶。
     */
    @Test
    public void testOfferExceedCapacityWithLowerValueUsingMaxHeap() {
        // 初始化大顶堆，容量为 5
        GenericHeap<Integer> maxHeap = new GenericHeap<>(5, Comparator.reverseOrder());

        // 插入一些较大的元素
        maxHeap.offer(50);
        maxHeap.offer(10);
        maxHeap.offer(30);
        maxHeap.offer(40);
        maxHeap.offer(20);



        // 堆已满，尝试插入一个更小的元素
        maxHeap.offer(5); // 5 的值比堆顶 50 更小（优先级更高）
        assertEquals(5, maxHeap.size());
        assertEquals(40, maxHeap.peek()); // 新的堆顶应该是堆中的最大值 40（前 5 小的元素中最大）

        // 再插入一个更小的元素
        maxHeap.offer(1); // 1 的值比堆顶 40 更小（优先级更高）
        assertEquals(5, maxHeap.size());
        assertEquals(30, maxHeap.peek()); // 堆顶更新为当前 5 个元素中的最大值 30
    }


    @Test
    public void testConstructHeapFromList() {
        GenericHeap<Integer> heapFromList = new GenericHeap<>(Arrays.asList(10, 5, 20, 15, 1), 5, Comparator.naturalOrder());

        assertEquals(5, heapFromList.size());
        assertEquals(1, heapFromList.peek()); // 最小堆，应保持最小值在堆顶
    }

    @Test
    public void testMaxHeapBehavior() {
        maxHeap.push(10);
        maxHeap.push(5);
        maxHeap.push(20);

        assertEquals(20, maxHeap.peek()); // 最大堆，堆顶应为最大值
        assertEquals(20, maxHeap.pop());
        assertEquals(10, maxHeap.pop());
        assertEquals(5, maxHeap.pop());
        assertTrue(maxHeap.isEmpty());
    }

    @Test
    public void testIsEmpty() {
        assertTrue(minHeap.isEmpty());

        minHeap.push(10);
        assertFalse(minHeap.isEmpty());
    }

    @Test
    public void testSize() {
        assertEquals(0, minHeap.size());

        minHeap.push(10);
        minHeap.push(20);
        assertEquals(2, minHeap.size());
    }
}

