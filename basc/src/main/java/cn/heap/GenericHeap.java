package cn.heap;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class GenericHeap<T> {
    private final List<T> heap;
    private final int capacity;
    private final Comparator<? super T> comparator;

    public GenericHeap(int capacity, Comparator<? super T> comparator) {
        this.capacity = capacity;
        this.heap = new ArrayList<>(capacity);
        this.comparator = comparator;
    }

    /* 构造方法，根据输入列表建堆 */
    public GenericHeap(List<T> items, int capacity, Comparator<? super T> comparator) {
        this.capacity = capacity;
        this.heap = new ArrayList<>(items);
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
    public T peek() {
        if (isEmpty()) {
            throw new IndexOutOfBoundsException("Heap is empty.");
        }
        return heap.get(0);
    }

    /* 元素入堆 */
    public void push(T val) {
        if (size() >= capacity) {
            throw new IllegalStateException("Heap capacity exceeded.");
        }
        heap.add(val);
        siftUp(size() - 1);
    }

    /* 在达到容量时移除优先级最低的元素。这种逻辑适用于有限优先队列 */
    public void offer(T val) {
        if (size() < capacity) {
            // 队列未满，直接添加
            push(val);
        } else if (comparator.compare(val, peek()) > 0) {
            // 队列已满，且新元素优先级高于堆顶，替换堆顶
            pop(); // 移除优先级最低的元素
            push(val); // 添加新元素
        }
    }

    /* 元素出堆 */
    public T pop() {
        if (isEmpty()) {
            throw new IndexOutOfBoundsException("Heap is empty.");
        }
        swap(0, size() - 1);
        T val = heap.remove(size() - 1);
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
        T temp = heap.get(i);
        heap.set(i, heap.get(j));
        heap.set(j, temp);
    }
}
