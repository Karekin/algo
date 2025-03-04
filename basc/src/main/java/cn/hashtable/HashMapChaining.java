package cn.hashtable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 这个哈希表实现确实体现了链式地址法（Chaining），因为它采用了拉链法来处理哈希冲突。
 * 具体来说，它使用了一个 List<List<Pair>> buckets 作为存储结构，其中：

 * buckets 是一个数组（ArrayList），其索引由 hashFunc(key) 计算得到。
 * 每个 bucket 是一个链表（ArrayList<Pair>），其中存储了相同哈希值的键值对（即发生冲突的键值对会存储在同一个 bucket 里）。
 * put 方法在 bucket 内部进行查找，如果 key 存在，则更新值；否则，在 bucket 尾部插入新的 Pair，这体现了链式地址法的存储方式。
 * get 方法遍历 bucket 查找匹配的 key，如果找到则返回 val，否则返回 null。
 * remove 方法遍历 bucket，找到匹配的 key 后将其从 bucket 中移除。
 */

/* 链式地址哈希表 */
public class HashMapChaining {
    int size; // 键值对数量
    int capacity; // 哈希表容量
    double loadThres; // 触发扩容的负载因子阈值
    int extendRatio; // 扩容倍数
    List<List<Pair>> buckets; // 桶数组

    /* 构造方法 */
    public HashMapChaining() {
        size = 0;
        capacity = 4;
        loadThres = 2.0 / 3.0;
        extendRatio = 2;
        buckets = new ArrayList<>(capacity);
        for (int i = 0; i < capacity; i++) {
            buckets.add(new ArrayList<>());
        }
    }

    /* 哈希函数 */
    int hashFunc(int key) {
        return key % capacity;
    }

    /* 负载因子 */
    double loadFactor() {
        return (double) size / capacity;
    }

    /* 查询操作 */
    public String get(int key) {
        int index = hashFunc(key);
        List<Pair> bucket = buckets.get(index);
        // 遍历桶，若找到 key ，则返回对应 val
        for (Pair pair : bucket) {
            if (pair.key == key) {
                return pair.val;
            }
        }
        // 若未找到 key ，则返回 null
        return null;
    }

    /* 添加操作 */
    public void put(int key, String val) {
        // 当负载因子超过阈值时，执行扩容
        if (loadFactor() > loadThres) {
            extend();
        }
        int index = hashFunc(key);
        List<Pair> bucket = buckets.get(index);
        // 遍历桶，若遇到指定 key ，则更新对应 val 并返回
        for (Pair pair : bucket) {
            if (pair.key == key) {
                pair.val = val;
                return;
            }
        }
        // 若无该 key ，则将键值对添加至尾部
        Pair pair = new Pair(key, val);
        bucket.add(pair);
        size++;
    }

    /* 删除操作 */
    public void remove(int key) {
        int index = hashFunc(key);
        List<Pair> bucket = buckets.get(index);
        // 遍历桶，从中删除键值对
        for (Pair pair : bucket) {
            if (pair.key == key) {
                bucket.remove(pair);
                size--;
                break;
            }
        }
    }

    /* 扩容哈希表 */
    void extend() {
        // 暂存原哈希表
        List<List<Pair>> bucketsTmp = buckets;
        // 初始化扩容后的新哈希表
        capacity *= extendRatio;
        buckets = new ArrayList<>(capacity);
        for (int i = 0; i < capacity; i++) {
            buckets.add(new ArrayList<>());
        }
        size = 0;
        // 将键值对从原哈希表搬运至新哈希表
        for (List<Pair> bucket : bucketsTmp) {
            for (Pair pair : bucket) {
                put(pair.key, pair.val);
            }
        }
    }

    /* 打印哈希表 */
    void print() {
        for (List<Pair> bucket : buckets) {
            List<String> res = new ArrayList<>();
            for (Pair pair : bucket) {
                res.add(pair.key + " -> " + pair.val);
            }
            System.out.println(res);
        }
    }

    public int getCapacity() {
        return capacity;
    }


    public static class Pair {
        public int key;
        public String val;

        public Pair(int key, String val) {
            this.key = key;
            this.val = val;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ArrayHashMap.Pair)) return false;
            ArrayHashMap.Pair pair = (ArrayHashMap.Pair) o;
            return key == pair.key && Objects.equals(val, pair.val);
        }

        @Override
        public int hashCode() {
            return Objects.hash(key, val);
        }
    }
}
