package cn.iocoder.boot.heap;

import cn.heap.TopKFinder;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class TopKFinderTest {

    @Test
    void testFindTopK() {
        int[] nums = {3, 10, 5, 20, 7, 6, 15};
        int k = 3;

        List<Integer> result = TopKFinder.findTopK(nums, k);

        // 验证结果是否为最大3个元素，并从大到小排序
        List<Integer> expected = Arrays.asList(20, 15, 10);
        assertEquals(expected, result);
    }

    @Test
    void testFindTopKWithKGreaterThanArrayLength() {
        int[] nums = {1, 2, 3};
        int k = 5;

        List<Integer> result = TopKFinder.findTopK(nums, k);

        // 验证结果应包含数组中所有元素，从大到小排序
        List<Integer> expected = Arrays.asList(3, 2, 1);
        assertEquals(expected, result);
    }

    @Test
    void testFindTopKWithEmptyArray() {
        int[] nums = {};
        int k = 3;

        List<Integer> result = TopKFinder.findTopK(nums, k);

        // 验证结果应为空，因为输入数组为空
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindLowK() {
        int[] nums = {3, 10, 5, 20, 7, 6, 15};
        int k = 3;

        List<Integer> result = TopKFinder.findLowK(nums, k);

        // 验证结果是否为最小3个元素，并从小到大排序
        List<Integer> expected = Arrays.asList(3, 5, 6);
        assertEquals(expected, result);
    }

    @Test
    void testFindLowKWithKGreaterThanArrayLength() {
        int[] nums = {1, 2, 3};
        int k = 5;

        List<Integer> result = TopKFinder.findLowK(nums, k);

        // 验证结果应包含数组中所有元素，从小到大排序
        List<Integer> expected = Arrays.asList(1, 2, 3);
        assertEquals(expected, result);
    }

    @Test
    void testFindLowKWithEmptyArray() {
        int[] nums = {};
        int k = 3;

        List<Integer> result = TopKFinder.findLowK(nums, k);

        // 验证结果应为空，因为输入数组为空
        assertTrue(result.isEmpty());
    }

    @Test
    void testTopKHeap() {
        int[] nums = {3, 10, 5, 20, 7, 6, 15};
        int k = 3;

        Queue<Integer> result = TopKFinder.topKHeap(nums, k);

        // 将 PriorityQueue 转为排序列表以便验证结果
        List<Integer> resultList = result.stream().sorted((a, b) -> b - a).collect(Collectors.toList());

        // 验证结果中的元素是否为最大3个，并从大到小排序
        List<Integer> expected = Arrays.asList(20, 15, 10);
        assertEquals(expected, resultList);
    }

    @Test
    void testTopKHeapWithKGreaterThanArrayLength() {
        int[] nums = {1, 2, 3};
        int k = 5;

        Queue<Integer> result = TopKFinder.topKHeap(nums, k);

        // 将 PriorityQueue 转为排序列表以便验证结果
        List<Integer> resultList = result.stream().sorted((a, b) -> b - a).collect(Collectors.toList());

        // 验证结果应包含数组中所有元素，从大到小排序
        List<Integer> expected = Arrays.asList(3, 2, 1);
        assertEquals(expected, resultList);
    }

    @Test
    void testTopKHeapWithEmptyArray() {
        int[] nums = {};
        int k = 3;

        Queue<Integer> result = TopKFinder.topKHeap(nums, k);

        // 验证结果应为空，因为输入数组为空
        assertTrue(result.isEmpty());
    }
}
