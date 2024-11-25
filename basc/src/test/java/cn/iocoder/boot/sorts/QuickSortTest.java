package cn.iocoder.boot.sorts;

import cn.sorts.QuickSort;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class QuickSortTest {

    @Test
    void testSortArray() {
        QuickSort quickSort = new QuickSort();

        // 测试空数组
        int[] emptyArray = {};
        assertArrayEquals(new int[]{}, quickSort.sortArray(emptyArray));

        // 测试单元素数组
        int[] singleElementArray = {1};
        assertArrayEquals(new int[]{1}, quickSort.sortArray(singleElementArray));

        // 测试已排序数组
        int[] sortedArray = {1, 2, 3, 4, 5};
        assertArrayEquals(new int[]{1, 2, 3, 4, 5}, quickSort.sortArray(sortedArray));

        // 测试逆序数组
        int[] reverseArray = {5, 4, 3, 2, 1};
        assertArrayEquals(new int[]{1, 2, 3, 4, 5}, quickSort.sortArray(reverseArray));

        // 测试包含重复元素的数组
        int[] duplicateArray = {3, 1, 2, 1, 3};
        assertArrayEquals(new int[]{1, 1, 2, 3, 3}, quickSort.sortArray(duplicateArray));

        // 测试随机数组
        int[] randomArray = {4, 2, 5, 3, 1};
        assertArrayEquals(new int[]{1, 2, 3, 4, 5}, quickSort.sortArray(randomArray));

        // 测试包含负数的数组
        int[] negativeArray = {3, -1, -2, 5, 0};
        assertArrayEquals(new int[]{-2, -1, 0, 3, 5}, quickSort.sortArray(negativeArray));
    }
}
