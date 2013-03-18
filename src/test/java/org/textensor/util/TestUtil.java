package org.textensor.util;

import java.util.Arrays;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public abstract class TestUtil {

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
}
