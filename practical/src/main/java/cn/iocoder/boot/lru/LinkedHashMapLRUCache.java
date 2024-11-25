package cn.iocoder.boot.lru;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 继承 LinkedHashMap 并重写 removeEldestEntry：
 * 直接继承 LinkedHashMap。
 * 重写 removeEldestEntry 方法来控制容量。
 * 使用 LinkedHashMap 的内置功能，代码简单易维护。
 */
class LinkedHashMapLRUCache extends LinkedHashMap<Integer, Integer> {
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;
    private final int capacity;

    public LinkedHashMapLRUCache(int capacity) {
        super(capacity, DEFAULT_LOAD_FACTOR, true);
        this.capacity = capacity;
    }

    public int get(int key) {
        return super.getOrDefault(key, -1);
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<Integer, Integer> eldest) {
        return size() > capacity;
    }
}