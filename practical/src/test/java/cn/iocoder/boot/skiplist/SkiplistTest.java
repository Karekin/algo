package cn.iocoder.boot.skiplist;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SkiplistTest {

    @Test
    void testAddAndSearch() {
        Skiplist skiplist = new Skiplist();

        // 插入一些值
        skiplist.add(1);
        skiplist.add(2);
        skiplist.add(3);

        // 验证插入后能找到这些值
        assertTrue(skiplist.search(1)); // 存在
        assertTrue(skiplist.search(2)); // 存在
        assertTrue(skiplist.search(3)); // 存在

        // 验证未插入的值
        assertFalse(skiplist.search(4)); // 不存在
        assertFalse(skiplist.search(-1)); // 不存在
    }

    @Test
    void testDuplicateAdd() {
        Skiplist skiplist = new Skiplist();

        // 插入重复值
        skiplist.add(1);
        skiplist.add(1);

        // 重复插入不会影响查找
        assertTrue(skiplist.search(1)); // 存在
    }

    @Test
    void testErase() {
        Skiplist skiplist = new Skiplist();

        // 插入并删除一些值
        skiplist.add(1);
        skiplist.add(2);
        skiplist.add(3);

        // 删除值 2
        assertTrue(skiplist.erase(2)); // 删除成功
        assertFalse(skiplist.search(2)); // 不存在

        // 删除不存在的值
        assertFalse(skiplist.erase(4)); // 删除失败
        assertFalse(skiplist.erase(-1)); // 删除失败

        // 删除其他值
        assertTrue(skiplist.erase(1)); // 删除成功
        assertFalse(skiplist.search(1)); // 不存在
    }

    @Test
    void testCombination() {
        Skiplist skiplist = new Skiplist();

        // 插入 1, 2, 3
        skiplist.add(1);
        skiplist.add(2);
        skiplist.add(3);

        // 删除 3，并验证
        assertTrue(skiplist.erase(3)); // 删除成功
        assertFalse(skiplist.search(3)); // 不存在

        // 验证其他值未受影响
        assertTrue(skiplist.search(1)); // 存在
        assertTrue(skiplist.search(2)); // 存在

        // 插入 4, 5
        skiplist.add(4);
        skiplist.add(5);

        // 验证新增值存在
        assertTrue(skiplist.search(4)); // 存在
        assertTrue(skiplist.search(5)); // 存在
    }

    @Test
    void testEmptySkiplist() {
        Skiplist skiplist = new Skiplist();

        // 验证空跳表操作
        assertFalse(skiplist.search(1)); // 不存在
        assertFalse(skiplist.erase(1)); // 删除失败
    }
}
