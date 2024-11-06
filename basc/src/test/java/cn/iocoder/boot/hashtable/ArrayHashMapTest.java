package cn.iocoder.boot.hashtable;

import cn.hashtable.ArrayHashMap;
import cn.hashtable.ArrayHashMap.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ArrayHashMapTest {
    private ArrayHashMap hashMap;

    // 每个测试方法执行前初始化一个新的 ArrayHashMap 实例
    @BeforeEach
    public void setUp() {
        hashMap = new ArrayHashMap();
    }

    // 测试添加和查询操作，包括哈希冲突情况下的覆盖行为
    @Test
    public void testPutAndGet() {
        hashMap.put(1, "A"); // 添加键值对 (1, "A")
        hashMap.put(2, "B"); // 添加键值对 (2, "B")
        hashMap.put(102, "C"); // 102 会产生哈希冲突，与键 2 产生相同的哈希索引

        // 测试查询操作
        assertEquals("A", hashMap.get(1), "键 1 的值应为 A");
        assertNull(hashMap.get(2), "键 2 的值应为 null，因为已被键 102 替换");
        assertEquals("C", hashMap.get(102), "键 102 的值应为 C，因为发生冲突后覆盖了键 2");
    }

    // 测试删除操作
    @Test
    public void testRemove() {
        hashMap.put(1, "A");
        hashMap.put(2, "B");

        // 删除键 1 后，应返回 null
        hashMap.remove(1);
        assertNull(hashMap.get(1), "键 1 应已被删除");
        // 键 2 仍然存在
        assertEquals("B", hashMap.get(2), "键 2 的值应为 B");

        // 删除键 2 后，应返回 null
        hashMap.remove(2);
        assertNull(hashMap.get(2), "键 2 应已被删除");
    }

    // 测试获取所有键值对
    @Test
    public void testPairSet() {
        hashMap.put(1, "A");
        hashMap.put(2, "B");
        hashMap.put(3, "C");

        List<Pair> pairSet = hashMap.pairSet();
        assertEquals(3, pairSet.size(), "键值对集合应包含 3 对键值");

        // 验证每个键值对是否存在
        assertTrue(pairSet.contains(new Pair(1, "A")), "键值对 (1, A) 应存在");
        assertTrue(pairSet.contains(new Pair(2, "B")), "键值对 (2, B) 应存在");
        assertTrue(pairSet.contains(new Pair(3, "C")), "键值对 (3, C) 应存在");
    }

    // 测试获取所有键
    @Test
    public void testKeySet() {
        hashMap.put(1, "A");
        hashMap.put(2, "B");
        hashMap.put(3, "C");

        List<Integer> keySet = hashMap.keySet();
        assertEquals(3, keySet.size(), "键集合应包含 3 个键");

        // 验证每个键是否存在
        assertTrue(keySet.contains(1), "键 1 应存在");
        assertTrue(keySet.contains(2), "键 2 应存在");
        assertTrue(keySet.contains(3), "键 3 应存在");
    }

    // 测试获取所有值
    @Test
    public void testValueSet() {
        hashMap.put(1, "A");
        hashMap.put(2, "B");
        hashMap.put(3, "C");

        List<String> valueSet = hashMap.valueSet();
        assertEquals(3, valueSet.size(), "值集合应包含 3 个值");

        // 验证每个值是否存在
        assertTrue(valueSet.contains("A"), "值 A 应存在");
        assertTrue(valueSet.contains("B"), "值 B 应存在");
        assertTrue(valueSet.contains("C"), "值 C 应存在");
    }

    // 测试哈希冲突情况
    @Test
    public void testHashCollision() {
        hashMap.put(1, "A");
        hashMap.put(101, "B"); // 101 与 1 产生相同的哈希索引

        // 由于冲突，键 1 的值被覆盖为 "B"
        assertEquals("B", hashMap.get(1), "键 1 的值应被覆盖为 B");
        assertEquals("B", hashMap.get(101), "键 101 的值应为 B");
    }

    // 测试打印方法
    @Test
    public void testPrint() {
        hashMap.put(1, "A");
        hashMap.put(2, "B");
        hashMap.put(3, "C");

        // 执行打印操作
        System.out.println("哈希表内容：");
        hashMap.print();
    }
}
