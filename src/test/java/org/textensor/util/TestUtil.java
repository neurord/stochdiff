package org.textensor.util;

import java.util.Arrays;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.*;

public class TestUtil {
    static final Logger log = LogManager.getLogger(TestUtil.class);

    public static boolean equal(double[] a, double[] b) {
        if (a.length != b.length)
            return false;
        for (int i = 0; i < a.length; i++)
            if (a[i] != b[i])
                return false;
        return true;
    }

    public static boolean equal(int[] a, int[] b) {
        if (a.length != b.length)
            return false;
        for (int i = 0; i < a.length; i++)
            if (a[i] != b[i])
                return false;
        return true;
    }

    public static void assertArrayEquals(double[] a, double[] b) {
        if (!equal(a, b))
            throw new AssertionError("two arrays are not equal: " +
                                     Arrays.toString(a) + ", " +
                                     Arrays.toString(b));
    }

    public static void assertArrayEquals(int[] a, int[] b) {
        if (!equal(a, b))
            throw new AssertionError("two arrays are not equal: " +
                                     Arrays.toString(a) + ", " +
                                     Arrays.toString(b));
    }

    public static void assertApproxEquals(double a, double b,
                                          double relative, double absolute) {
        double diff = Math.abs(a-b);
        if (absolute >= 0 && diff <= absolute)
            return;
        if (relative >= 0 && diff <= relative * a || diff <= relative * b)
            return;
        throw new AssertionError("two numbers are not equal enough: " + a + ", " + b
                                 + " rel=" + relative + " abs=" + absolute);
    }

    /**
     * "Multiply" Object lists: all items from the first cell in the
     * first one, concatenated with all items from the first cell in the
     * second one, ..., all items from the second cell in the first one,
     * ...
     */
    public static Object[][] multiply(Object[]... items) {
        return _multiply(new Object[][] {{}}, items);
    }

    public static Object[][] _multiply(Object[][] head, Object[]... rest) {
        if (rest.length == 0)
            return head;

        int n = head.length * rest[0].length;
        int i = 0;
        Object[][] t = new Object[n][];

        Object[] b = rest[0];
        for (Object[] a: head)
            for (Object bx: b) {
                final Object[] c;
                if (bx.getClass().isArray()) {
                    Object[] bxa = (Object[]) bx;
                    c = Arrays.copyOf(a, a.length + bxa.length);
                    System.arraycopy(bxa, 0, c, a.length, bxa.length);
                } else {
                    c = Arrays.copyOf(a, a.length + 1);
                    c[a.length] = bx;
                }
                t[i++] = c;
            }

        if (rest.length == 1)
            /* work around java bug: Arrays.copyOfRange(,,0) doesn't work */
            return t;
        else
            return _multiply(t, Arrays.copyOfRange(rest, 1, rest.length));
    }

    @Test
    public static void test__multiply_two() {
        Object[][] expect = new Object[][] {
            {1, 2, 3},
        };
        Object[][] actual = _multiply(new Object[][]{{1,2}},
                                      new Object[]{3});
        log.info("actual: {}, expect: {}", actual, expect);
        assertTrue(Arrays.deepEquals(actual, expect));
    }

    @Test
    public static void test__multiply_three() {
        Object[][] expect = new Object[][] {
            {1, 2, 3, 4},
        };
        Object[][] actual = _multiply(new Object[][]{{1,2}},
                                      new Object[]{3},
                                      new Object[]{4});
        log.info("actual: {}, expect: {}", actual, expect);
        assertTrue(Arrays.deepEquals(actual, expect));
    }

    @Test
    public static void test__multiply_singleend() {
        Object[][] expect = new Object[][] {
            {1, 2, 3, 4, 5},
        };
        Object[][] actual = _multiply(new Object[][]{{1,2}},
                                      new Object[]{3},
                                      new Object[][]{{4, 5}});
        log.info("actual: {}, expect: {}", actual, expect);
        assertTrue(Arrays.deepEquals(actual, expect));
    }

    @Test
    public static void test__multiply_multiend() {
        Object[][] expect = new Object[][] {
            {1, 2, 3, 4}, {1, 2, 3, 5},
        };
        Object[][] actual = _multiply(new Object[][]{{1,2}},
                                      new Object[]{3},
                                      new Object[]{4, 5});
        log.info("actual: {}, expect: {}", actual, expect);
        assertTrue(Arrays.deepEquals(actual, expect));
    }

    @Test
    public static void test_multiply_basic() {
        Object[][] expect = new Object[][] {
            {1, 2, 3},
        };
        Object[][] actual = multiply(
                                     new Object[][]{
                                         {1},
                                         {2},
                                         {3}});
        log.info("actual: {}, expect: {}", actual, expect);
        assertTrue(Arrays.deepEquals(actual, expect));
    }

    @Test
    public static void test_multiply() {
        Object[][] expect = new Object[][] {
            {1, 2, 'a', 4},
            {1, 2, 'b', 4},
        };
        Object[][] actual = multiply(new Object[][]{ {1},
                                                     {2},
                                                     {'a', 'b'},
                                                     {4} });
        log.info("actual: {}, expect: {}", actual, expect);
        assertTrue(Arrays.deepEquals(actual, expect));
    }
}
