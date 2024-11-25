package cn.sorts;

public class QuickSort {
    public int[] sortArray(int[] nums) {
        quickSort(nums, 0, nums.length - 1);
        return nums;
    }

    /* 快速排序（简化） */
    void quickSort(int[] nums, int left, int right) {
        // 子数组长度为 1 时终止递归
        if (left >= right)
            return;
        // 哨兵划分
        int pivot = partition(nums, left, right);
        // 递归左子数组、右子数组
        quickSort(nums, left, pivot - 1);
        quickSort(nums, pivot + 1, right);
    }

    /* 快速排序（尾递归优化） */
//    void quickSort(int[] nums, int left, int right) {
//        // 子数组长度为 1 时终止
//        while (left < right) {
//            // 哨兵划分操作
//            int pivot = partition(nums, left, right);
//            // 对两个子数组中较短的那个执行快速排序
//            if (pivot - left < right - pivot) {
//                quickSort(nums, left, pivot - 1); // 递归排序左子数组
//                left = pivot + 1; // 剩余未排序区间为 [pivot + 1, right]
//            } else {
//                quickSort(nums, pivot + 1, right); // 递归排序右子数组
//                right = pivot - 1; // 剩余未排序区间为 [left, pivot - 1]
//            }
//        }
//    }

    /* 哨兵划分（简化） */
    int partition(int[] nums, int left, int right) {
        // 以 nums[left] 为基准数
        int i = left, j = right;
        while (i < j) {
            while (i < j && nums[j] >= nums[left])
                j--;          // 从右向左找首个小于基准数的元素
            while (i < j && nums[i] <= nums[left])
                i++;          // 从左向右找首个大于基准数的元素
            swap(nums, i, j); // 交换这两个元素
        }
        swap(nums, i, left);  // 将基准数交换至两子数组的分界线
        return i;             // 返回基准数的索引（left 是固定的起始位置，而不是pivot的位置）
    }

    /* 哨兵划分（三数取中值优化） */
//    int partition(int[] nums, int left, int right) {
//        // 选取三个候选元素的中位数
//        int med = medianThree(nums, left, (left + right) / 2, right);
//        // 将中位数交换至数组最左端
//        swap(nums, left, med);
//        // 以 nums[left] 为基准数
//        int i = left, j = right;
//        while (i < j) {
//            while (i < j && nums[j] >= nums[left])
//                j--;          // 从右向左找首个小于基准数的元素
//            while (i < j && nums[i] <= nums[left])
//                i++;          // 从左向右找首个大于基准数的元素
//            swap(nums, i, j); // 交换这两个元素
//        }
//        swap(nums, i, left);  // 将基准数交换至两子数组的分界线
//        return i;             // 返回基准数的索引
//    }

    /* 选取三个候选元素的中位数 */
    int medianThree(int[] nums, int left, int mid, int right) {
        // 此处使用异或运算来简化代码
        // 异或规则为 0 ^ 0 = 1 ^ 1 = 0, 0 ^ 1 = 1 ^ 0 = 1
        if ((nums[left] < nums[mid]) ^ (nums[left] < nums[right]))
            return left;
        else if ((nums[mid] < nums[left]) ^ (nums[mid] < nums[right]))
            return mid;
        else
            return right;
    }


    /* 元素交换 */
    void swap(int[] nums, int i, int j) {
        int tmp = nums[i];
        nums[i] = nums[j];
        nums[j] = tmp;
    }

}
