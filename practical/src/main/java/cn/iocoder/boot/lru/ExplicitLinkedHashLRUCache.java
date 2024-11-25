package cn.iocoder.boot.lru;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 基于 LinkedHashMap 的显式操作：
 * 使用 LinkedHashMap 手动管理元素的顺序。
 * 每次访问或插入时，通过显式的 remove 和 put 来移动元素。
 */
class ExplicitLinkedHashLRUCache {
    private final int capacity;
    private final Map<Integer, Integer> cache = new LinkedHashMap<>(); // 自带双向链表

    public ExplicitLinkedHashLRUCache(int capacity) {
        this.capacity = capacity;
    }

    public int get(int key) {
        // 删除 key，并利用返回值判断 key 是否在 cache 中
        Integer value = cache.remove(key);
        if (value != null) { // key 在 cache 中
            cache.put(key, value); // 把 key 移到链表末尾
            return value;
        }
        // key 不在 cache 中
        return -1;
    }

    public void put(int key, int value) {
        // 如果容量为 0，直接返回，不允许存储任何元素
        if (capacity == 0) {
            return;
        }

        // 删除 key，并利用返回值判断 key 是否在 cache 中
        if (cache.remove(key) != null) { // key 在 cache 中
            cache.put(key, value); // 把 key 移到链表末尾
            return;
        }
        // key 不在 cache 中，那么就把 key 插入 cache，插入前判断 cache 是否满了
        if (cache.size() == capacity) { // cache 满了
            Integer oldestKey = cache.keySet().iterator().next();
            cache.remove(oldestKey); // 移除最久未使用 key
        }
        cache.put(key, value);
    }
}