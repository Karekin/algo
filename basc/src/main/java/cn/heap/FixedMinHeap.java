package cn.heap;

import java.util.ArrayList;
import java.util.List;

public class FixedMinHeap {
    private final List<Integer> minHeap;
    private final int capacity;

    public FixedMinHeap(int capacity) {
        this.capacity = capacity;
        this.minHeap = new ArrayList<>(capacity);
    }

    /* 构造方法，根据输入列表建堆 */
    public FixedMinHeap(List<Integer> nums, int capacity) {
        this.capacity = capacity;
        // 将列表元素原封不动添加进堆
        this.minHeap = new ArrayList<>(nums);
        // 堆化除叶节点以外的其他所有节点
        for (int i = parent(size() - 1); i >= 0; i--) {
            siftDown(i);
        }
    }

    /* 返回堆的大小 */
    public int size() {
        return minHeap.size();
    }

    /* 判断堆是否为空 */
    public boolean isEmpty() {
        return minHeap.isEmpty();
    }

    /* 访问堆顶元素 */
    public int peek() {
        if (isEmpty()) {
            throw new IndexOutOfBoundsException();
        }
        return minHeap.get(0);
    }

    /* 元素入堆 */
    public void push(int val) {
        if (size() >= capacity) {
            throw new IllegalStateException("Heap capacity exceeded");
        }
        // 添加节点
        minHeap.add(val);
        // 从底至顶堆化
        siftUp(size() - 1);
    }

    /* 元素出堆 */
    public int pop() {
        if (isEmpty()) {
            throw new IndexOutOfBoundsException();
        }

        // 交换根节点与最右叶节点（交换首元素与尾元素）
        swap(0, size() - 1);
        // 删除节点
        int val = minHeap.remove(size() - 1);

        // 从顶至底堆化
        siftDown(0);
        // 返回堆顶元素
        return val;
    }

    /* 从节点 i 开始，从顶至底堆化 */
    private void siftDown(int i) {
        while (true) {
            // 判断节点 i, l, r 中值最小的节点，记为 mi
            int l = left(i), r = right(i), mi = i;

            if (l < size() && minHeap.get(l) < minHeap.get(mi))
                mi = l;
            if (r < size() && minHeap.get(r) < minHeap.get(mi))
                mi = r;
            // 若节点 i 最小或索引 l, r 越界，则无须继续堆化，跳出
            if (mi == i)
                break;
            // 交换两节点
            swap(i, mi);
            // 循环向下堆化
            i = mi;
        }
    }

    /* 从节点 i 开始，从底至顶堆化 */
    private void siftUp(int i) {
        // 当“越过根节点”时，结束堆化
        while (i > 0) {
            // 获取节点 i 的父节点
            int p = parent(i);
            // 当“节点无须修复”时，结束堆化
            if (minHeap.get(i) >= minHeap.get(p))
                break;
            // 交换两节点
            swap(i, p);
            // 循环向上堆化
            i = p;
        }
    }

    /* 获取左子节点的索引 */
    private int left(int i) {
        return 2 * i + 1;
    }

    /* 获取右子节点的索引 */
    private int right(int i) {
        return 2 * i + 2;
    }

    /* 获取父节点的索引 */
    private int parent(int i) {
        return (i - 1) / 2;
    }

    /* 交换索引 i 和 j 的两个元素 */
    private void swap(int i, int j) {
        int temp = minHeap.get(i);
        minHeap.set(i, minHeap.get(j));
        minHeap.set(j, temp);
    }
}

