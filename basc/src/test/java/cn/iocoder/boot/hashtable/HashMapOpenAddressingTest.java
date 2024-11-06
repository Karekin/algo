package cn.iocoder.boot.hashtable;

import cn.hashtable.HashMapOpenAddressing;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class HashMapOpenAddressingTest {
    private HashMapOpenAddressing hashMap;

    // 在每个测试方法执行前初始化一个新的 HashMapOpenAddressing 实例
    @BeforeEach
    public void setUp() {
        hashMap = new HashMapOpenAddressing();
    }

    // 测试添加和查询操作
    @Test
    public void testPutAndGet() {
        hashMap.put(1, "A"); // 添加键值对 (1, "A")
        hashMap.put(2, "B"); // 添加键值对 (2, "B")
        hashMap.put(3, "C"); // 添加键值对 (3, "C")

        // 测试查询操作
        assertEquals("A", hashMap.get(1), "键 1 的值应为 A");
        assertEquals("B", hashMap.get(2), "键 2 的值应为 B");
        assertEquals("C", hashMap.get(3), "键 3 的值应为 C");
        assertNull(hashMap.get(4), "键 4 不存在，应返回 null");
    }

    // 测试删除操作
    @Test
    public void testRemove() {
        hashMap.put(1, "A");
        hashMap.put(2, "B");

        // 删除键 1，查询结果应为 null
        hashMap.remove(1);
        assertNull(hashMap.get(1), "键 1 应已被删除");
        // 键 2 应仍然存在
        assertEquals("B", hashMap.get(2), "键 2 的值应为 B");

        // 再次删除键 2，查询结果应为 null
        hashMap.remove(2);
        assertNull(hashMap.get(2), "键 2 应已被删除");
    }

    // 测试负载因子达到阈值时的扩容行为
    @Test
    public void testExtendCapacity() {
        // 在初始容量为 4 和负载因子阈值为 2/3 的情况下，添加第 4 个键值对会触发扩容
        hashMap.put(1, "A");
        hashMap.put(2, "B");
        hashMap.put(3, "C");
        hashMap.put(4, "D"); // 此处应触发扩容

        // 检查哈希表容量是否扩容至 8（4 * 2 = 8）
        assertEquals(8, hashMap.getCapacity(), "哈希表容量应扩容至 8");

        // 检查扩容后数据是否保持正确
        assertEquals("A", hashMap.get(1), "扩容后键 1 的值应为 A");
        assertEquals("B", hashMap.get(2), "扩容后键 2 的值应为 B");
        assertEquals("C", hashMap.get(3), "扩容后键 3 的值应为 C");
        assertEquals("D", hashMap.get(4), "扩容后键 4 的值应为 D");
    }


    // 测试哈希冲突处理，确保新的键值对正确插入
    @Test
    public void testHashCollision() {
        hashMap.put(1, "A");
        hashMap.put(5, "B"); // 键 5 与键 1 发生哈希冲突（假设初始容量 4）

        // 确保哈希冲突时数据不丢失
        assertEquals("A", hashMap.get(1), "键 1 的值应为 A");
        assertEquals("B", hashMap.get(5), "键 5 的值应为 B");
    }

    // 测试重复添加键值对时的覆盖行为
    @Test
    public void testOverrideValue() {
        hashMap.put(1, "A");
        hashMap.put(1, "B"); // 键 1 的值应被覆盖为 B

        // 验证覆盖后的值
        assertEquals("B", hashMap.get(1), "键 1 的值应被覆盖为 B");
    }

    // 测试删除标记的使用
    @Test
    public void testTombstoneReuse() {
        hashMap.put(1, "A");
        hashMap.remove(1); // 删除键 1，应该留下删除标记

        // 删除后添加新的键值对（使用相同哈希）
        hashMap.put(1, "B");
        assertEquals("B", hashMap.get(1), "应重用删除标记并正确插入新的键值对");
    }

    // 测试打印哈希表内容
    @Test
    public void testPrint() {
        hashMap.put(1, "A");
        hashMap.put(2, "B");
        hashMap.put(3, "C");

        // 调用打印方法，将哈希表内容输出到控制台
        System.out.println("哈希表内容：");
        hashMap.print();
    }
}

