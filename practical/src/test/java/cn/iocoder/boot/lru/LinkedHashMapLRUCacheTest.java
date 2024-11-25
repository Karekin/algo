package cn.iocoder.boot.lru;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LinkedHashMapLRUCacheTest {

    @Test
    void testCacheBasicOperations() {
        LinkedHashMapLRUCache cache = new LinkedHashMapLRUCache(2);

        // 测试插入和获取
        cache.put(1, 1);
        cache.put(2, 2);
        assertEquals(1, cache.get(1)); // 缓存中有 key=1，返回 1
        assertEquals(2, cache.get(2)); // 缓存中有 key=2，返回 2

        // 测试缓存容量限制
        cache.put(3, 3); // 插入 key=3，导致 key=1 被淘汰
        assertEquals(-1, cache.get(1)); // key=1 已被淘汰
        assertEquals(2, cache.get(2)); // key=2 仍在缓存中
        assertEquals(3, cache.get(3)); // key=3 仍在缓存中

        // 测试更新已有 key
        cache.put(2, 20); // 更新 key=2 的值
        assertEquals(20, cache.get(2)); // 确认值已更新
    }

    @Test
    void testCacheEvictionOrder() {
        LinkedHashMapLRUCache cache = new LinkedHashMapLRUCache(3);

        // 插入多个元素
        cache.put(1, 1);
        cache.put(2, 2);
        cache.put(3, 3);

        // 访问 key=1，更新其为最近使用
        assertEquals(1, cache.get(1));

        // 插入新的 key，导致最近未使用的 key=2 被淘汰
        cache.put(4, 4);
        assertEquals(-1, cache.get(2)); // key=2 已被淘汰
        assertEquals(1, cache.get(1)); // key=1 仍在缓存中
        assertEquals(3, cache.get(3)); // key=3 仍在缓存中
        assertEquals(4, cache.get(4)); // key=4 仍在缓存中
    }

    @Test
    void testEdgeCases() {
        // 测试容量为 1 的缓存
        LinkedHashMapLRUCache cache = new LinkedHashMapLRUCache(1);

        cache.put(1, 1);
        assertEquals(1, cache.get(1)); // 缓存中有 key=1

        cache.put(2, 2); // 插入新 key，导致 key=1 被淘汰
        assertEquals(-1, cache.get(1)); // key=1 已被淘汰
        assertEquals(2, cache.get(2)); // key=2 仍在缓存中

        // 测试容量为 0 的缓存
        LinkedHashMapLRUCache emptyCache = new LinkedHashMapLRUCache(0);
        emptyCache.put(1, 1);
        assertEquals(-1, emptyCache.get(1)); // 容量为 0，无法存储任何元素
    }

    @Test
    void testUpdateAccessOrder() {
        LinkedHashMapLRUCache cache = new LinkedHashMapLRUCache(2);

        cache.put(1, 1);
        cache.put(2, 2);
        assertEquals(1, cache.get(1)); // 访问 key=1
        cache.put(3, 3); // 插入 key=3，导致 key=2 被淘汰
        assertEquals(-1, cache.get(2)); // key=2 已被淘汰
        assertEquals(1, cache.get(1)); // key=1 仍在缓存中
        assertEquals(3, cache.get(3)); // key=3 仍在缓存中
    }
}

