package cn.iocoder.boot.stack;


import cn.stack.LinkedListStack;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LinkedListStackTest {
    private LinkedListStack stack;

    @BeforeEach
    void setUp() {
        stack = new LinkedListStack();
    }

    @Test
    void testIsEmptyOnNewStack() {
        assertTrue(stack.isEmpty(), "新栈应为空");
    }

    @Test
    void testPushAndSize() {
        stack.push(10);
        stack.push(20);
        stack.push(30);
        assertEquals(3, stack.size(), "入栈3个元素后栈大小应为3");
    }

    @Test
    void testPeek() {
        stack.push(10);
        stack.push(20);
        stack.push(30);
        assertEquals(30, stack.peek(), "栈顶元素应为30");
    }

    @Test
    void testPop() {
        stack.push(10);
        stack.push(20);
        stack.push(30);

        assertEquals(30, stack.pop(), "第一次出栈应返回30");
        assertEquals(20, stack.pop(), "第二次出栈应返回20");
        assertEquals(10, stack.pop(), "第三次出栈应返回10");
        assertTrue(stack.isEmpty(), "出栈后栈应为空");
    }

    @Test
    void testPopOnEmptyStack() {
        Exception exception = assertThrows(IndexOutOfBoundsException.class, stack::pop);
        assertEquals("Stack is empty.", exception.getMessage(), "空栈出栈应抛出异常");
    }

    @Test
    void testPeekOnEmptyStack() {
        Exception exception = assertThrows(IndexOutOfBoundsException.class, stack::peek);
        assertEquals("Stack is empty.", exception.getMessage(), "空栈取栈顶应抛出异常");
    }

    @Test
    void testToArray() {
        stack.push(40);
        stack.push(50);
        int[] expectedArray = {40, 50};
        assertArrayEquals(expectedArray, stack.toArray(), "toArray 方法返回值应匹配栈元素顺序");
    }
}


