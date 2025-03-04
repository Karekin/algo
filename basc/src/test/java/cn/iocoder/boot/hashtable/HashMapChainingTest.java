package cn.iocoder.boot.hashtable;

import cn.hashtable.HashMapChaining;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

class HashMapChainingTest {
    private HashMapChaining hashMap;

    @BeforeEach
    void setUp() {
        hashMap = new HashMapChaining();
    }

    @Test
    void testPutAndGet() {
        hashMap.put(1, "one");
        hashMap.put(2, "two");
        hashMap.put(3, "three");

        assertEquals("one", hashMap.get(1));
        assertEquals("two", hashMap.get(2));
        assertEquals("three", hashMap.get(3));
    }

    @Test
    void testUpdateValue() {
        hashMap.put(1, "one");
        hashMap.put(1, "updated-one");

        assertEquals("updated-one", hashMap.get(1));
    }

    @Test
    void testRemove() {
        hashMap.put(1, "one");
        hashMap.put(2, "two");
        hashMap.remove(1);

        assertNull(hashMap.get(1));
        assertEquals("two", hashMap.get(2));
    }

    @Test
    void testHashCollisionHandling() {
        int capacity = 4; // 初始容量
        int key1 = 1;
        int key2 = key1 + capacity; // 产生哈希冲突的 key

        hashMap.put(key1, "one");
        hashMap.put(key2, "collision");

        assertEquals("one", hashMap.get(key1));
        assertEquals("collision", hashMap.get(key2));
    }

    @Test
    void testExtendCapacity() {
        hashMap.put(1, "one");
        hashMap.put(2, "two");
        hashMap.put(3, "three");
        hashMap.put(4, "four"); // 触发扩容

        assertEquals("one", hashMap.get(1));
        assertEquals("two", hashMap.get(2));
        assertEquals("three", hashMap.get(3));
        assertEquals("four", hashMap.get(4));

        assertTrue(hashMap.getCapacity() >= 8); // 确保扩容发生
    }
}

