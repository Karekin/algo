package cn.heap;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class FixedHeap {
    private final List<Integer> heap;
    private final int capacity;
    private final Comparator<Integer> comparator;

    public FixedHeap(int capacity, Comparator<Integer> comparator) {
        this.capacity = capacity;
        this.heap = new ArrayList<>(capacity);
        this.comparator = comparator;
    }

    /* 构造方法，根据输入列表建堆 */
    public FixedHeap(List<Integer> nums, int capacity, Comparator<Integer> comparator) {
        this.capacity = capacity;
        this.heap = new ArrayList<>(nums);
        this.comparator = comparator;
        // 堆化除叶节点以外的其他所有节点
        for (int i = parent(size() - 1); i >= 0; i--) {
            siftDown(i);
        }
    }

    /* 返回堆的大小 */
    public int size() {
        return heap.size();
    }

    /* 判断堆是否为空 */
    public boolean isEmpty() {
        return heap.isEmpty();
    }

    /* 访问堆顶元素 */
    public int peek() {
        if (isEmpty()) {
            throw new IndexOutOfBoundsException();
        }
        return heap.get(0);
    }

    /* 元素入堆 */
    public void push(int val) {
        if (size() >= capacity) {
            throw new IllegalStateException("Heap capacity exceeded");
        }
        heap.add(val);
        siftUp(size() - 1);
    }

    /* 元素出堆 */
    public int pop() {
        if (isEmpty()) {
            throw new IndexOutOfBoundsException();
        }
        swap(0, size() - 1);
        int val = heap.remove(size() - 1);
        siftDown(0);
        return val;
    }

    /* 从节点 i 开始，从顶至底堆化 */
    private void siftDown(int i) {
        while (true) {
            int l = left(i), r = right(i), target = i;

            if (l < size() && comparator.compare(heap.get(l), heap.get(target)) < 0) {
                target = l;
            }
            if (r < size() && comparator.compare(heap.get(r), heap.get(target)) < 0) {
                target = r;
            }

            if (target == i) break;

            swap(i, target);
            i = target;
        }
    }

    /* 从节点 i 开始，从底至顶堆化 */
    private void siftUp(int i) {
        while (i > 0) {
            int p = parent(i);
            if (comparator.compare(heap.get(i), heap.get(p)) >= 0) break;
            swap(i, p);
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
        int temp = heap.get(i);
        heap.set(i, heap.get(j));
        heap.set(j, temp);
    }
}
