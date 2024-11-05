package cn.heap;

import java.util.ArrayList;
import java.util.List;

import java.util.Comparator;

public class TopKFinder {

    /* 查找数组中最大的 k 个元素 */
    public static List<Integer> findTopK(int[] nums, int k) {
        if (k >= nums.length) {
            List<Integer> result = new ArrayList<>();
            for (int num : nums) {
                result.add(num);
            }
            result.sort(Comparator.reverseOrder()); // 从大到小排序
            return result;
        }

        // 初始化容量为 k 的小顶堆
        GenericHeap<Integer> minHeap = new GenericHeap<>(k, Comparator.naturalOrder());

        // 将数组的前 k 个元素入堆
        for (int i = 0; i < k; i++) {
            minHeap.push(nums[i]);
        }

        // 从第 k+1 个元素开始，保持堆的长度为 k
        for (int i = k; i < nums.length; i++) {
            if (nums[i] > minHeap.peek()) {
                minHeap.pop();
                minHeap.push(nums[i]);
            }
        }

        // 将堆中的元素存入结果列表并从大到小排序
        List<Integer> result = new ArrayList<>();
        while (!minHeap.isEmpty()) {
            result.add(minHeap.pop());
        }
        result.sort(Comparator.reverseOrder()); // 从大到小排序
        return result;
    }

    /* 查找数组中最小的 k 个元素 */
    public static List<Integer> findLowK(int[] nums, int k) {
        if (k >= nums.length) {
            List<Integer> result = new ArrayList<>();
            for (int num : nums) {
                result.add(num);
            }
            result.sort(Comparator.naturalOrder()); // 从小到大排序
            return result;
        }

        // 初始化容量为 k 的大顶堆
        GenericHeap<Integer> maxHeap = new GenericHeap<>(k, Comparator.reverseOrder());

        // 将数组的前 k 个元素入堆
        for (int i = 0; i < k; i++) {
            maxHeap.push(nums[i]);
        }

        // 从第 k+1 个元素开始，保持堆的长度为 k
        for (int i = k; i < nums.length; i++) {
            if (nums[i] < maxHeap.peek()) {
                maxHeap.pop();
                maxHeap.push(nums[i]);
            }
        }

        // 将堆中的元素存入结果列表并从小到大排序
        List<Integer> result = new ArrayList<>();
        while (!maxHeap.isEmpty()) {
            result.add(maxHeap.pop());
        }
        result.sort(Comparator.naturalOrder()); // 从小到大排序
        return result;
    }

    public static void main(String[] args) {
        int[] nums = {3, 10, 5, 20, 7, 6, 15};
        int k = 3;

        List<Integer> topK = findTopK(nums, k);
        System.out.println("Top " + k + " elements: " + topK);

        List<Integer> lowK = findLowK(nums, k);
        System.out.println("Low " + k + " elements: " + lowK);
    }
}



