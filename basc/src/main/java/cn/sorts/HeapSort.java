package cn.sorts;

/**
 * 堆排序
 * 如何实现这种简化的逻辑？
 * 1. 输入数组并建立小顶堆，此时最小元素位于堆顶。
 * 2. 不断执行出堆操作，依次记录出堆元素，即可得到从小到大排序的序列。
 * 以上方法虽然可行，但需要借助一个额外数组来保存弹出的元素，比较浪费空间。
 * 在实际中，我们通常使用一种更加优雅的实现方式。
 */
public class HeapSort {
    public int[] sortArray(int[] nums) {
        // 调用堆排序方法对数组进行排序
        heapSort(nums);
        return nums; // 返回排序后的数组
    }

    /* 堆排序 */
    void heapSort(int[] nums) {
        // 建堆操作：堆化除叶节点以外的其他所有节点
        for (int i = parent(nums.length - 1); i >= 0; i--) {
            siftDown(nums, nums.length, i);
        }
        // 从堆中提取最大元素，循环 n-1 轮
        for (int i = nums.length - 1; i > 0; i--) {
            // 交换根节点与最右叶节点（交换首元素与尾元素）
            swap(nums, i, 0);
            // 以根节点为起点，从顶至底进行堆化
            siftDown(nums, i, 0);
        }
    }

    /* 堆的长度为 n ，从节点 i 开始，从顶至底堆化 */
    void siftDown(int[] nums, int n, int i) {
        while (true) {
            // 判断节点 i, l, r 中值最大的节点，记为 ma
            int l = left(i);
            int r = right(i);
            int ma = i;
            if (l < n && nums[l] > nums[ma])
                ma = l;
            if (r < n && nums[r] > nums[ma])
                ma = r;
            // 若节点 i 最大或索引 l, r 越界，则无须继续堆化，跳出
            if (ma == i)
                break;
            // 交换两节点
            swap(nums, ma, i);
            // 循环向下堆化
            i = ma;
        }
    }

    /* 元素交换 */
    void swap(int[] nums, int i, int j) {
        int tmp = nums[i];
        nums[i] = nums[j];
        nums[j] = tmp;
    }

    /* 获取左子节点的索引 */
    int left(int i) {
        return 2 * i + 1;
    }

    /* 获取右子节点的索引 */
    int right(int i) {
        return 2 * i + 2;
    }

    /* 获取父节点的索引 */
    int parent(int i) {
        return (i - 1) / 2; // 向下整除
    }
}