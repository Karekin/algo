package cn.iocoder.boot.lru;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CustomLinkedHashLRUCacheTest {

    /**
     * 在 LRU 缓存中，每次访问或插入时，都会将对应的键移动到最近使用的位置。
     * 当插入新键导致容量超出限制时，会淘汰最近最少使用的键。
     */
    @Test
    void testCacheBasicOperations() {
        CustomLinkedHashLRUCache cache = new CustomLinkedHashLRUCache(2);

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
        cache.put(2, 10); // 更新 key=2 的值
        assertEquals(10, cache.get(2)); // 确认值已更新
        cache.put(4, 4); // 插入 key=4，导致 key=3 被淘汰
        assertEquals(-1, cache.get(3)); // key=3 已被淘汰
        assertEquals(4, cache.get(4)); // key=4 仍在缓存中
    }


    @Test
    void testCacheEvictionOrder() {
        CustomLinkedHashLRUCache cache = new CustomLinkedHashLRUCache(3);

        // 插入多个元素
        cache.put(1, 1);
        cache.put(2, 2);
        cache.put(3, 3);

        // 访问 key=1，更新其为最近使用
        cache.get(1);

        // 插入新的 key，导致最近未使用的 key=2 被淘汰
        cache.put(4, 4);

        // 验证 key=2 被淘汰
        assertEquals(-1, cache.get(2));
        assertEquals(1, cache.get(1)); // key=1 仍在缓存中
        assertEquals(3, cache.get(3)); // key=3 仍在缓存中
        assertEquals(4, cache.get(4)); // key=4 仍在缓存中
    }

    @Test
    void testCacheEdgeCases() {
        // 测试容量为 1 的缓存
        CustomLinkedHashLRUCache cache = new CustomLinkedHashLRUCache(1);

        cache.put(1, 1);
        assertEquals(1, cache.get(1)); // 缓存中有 key=1

        cache.put(2, 2); // 插入新 key，导致 key=1 被淘汰
        assertEquals(-1, cache.get(1)); // key=1 已被淘汰
        assertEquals(2, cache.get(2)); // key=2 仍在缓存中

        // 测试空缓存
        CustomLinkedHashLRUCache emptyCache = new CustomLinkedHashLRUCache(0);
        emptyCache.put(1, 1);
        assertEquals(-1, emptyCache.get(1)); // 容量为 0，无法存储任何元素
    }
}

