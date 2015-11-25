package neurord.util;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import static org.testng.Assert.assertEquals;
import static neurord.util.TestUtil.assertArrayEquals;
import org.testng.annotations.*;

public class TestArrayUtil {

    @DataProvider
    public Object[][] arrays_double() {
        return new Object[][] {
            /* longer first */
            new Object[] { new double[][]{ new double[]{1, 2, 3},
                                           new double[]{3, 5}},
                           3,
                           new double[]{ 1, 2, 3, 3, 5, 0}},
            /* empty */
            new Object[] { new double[][]{ },
                           0,
                           new double[]{ } },

            /* empty */
            new Object[] { new double[][]{ new double[] {},
                                           new double[] {}},
                           0,
                           new double[]{ } },
        };
    }

    @Test(dataProvider="arrays_double")
    void testMaxLength_double(double[][] ar, int columns, double[] flat) {
        assertEquals(ArrayUtil.maxLength(ar), columns);
    }

    @Test(dataProvider="arrays_double")
    void testFlatten_double(double[][] ar, int columns, double[] flat) {
        assertArrayEquals(ArrayUtil.flatten(ar, columns), flat);
    }

    @DataProvider
    public Object[][] arrays_int() {
        return new Object[][] {
            /* longer first */
            new Object[] { new int[][]{ new int[]{1, 2, 3},
                                           new int[]{3, 5}},
                           3,
                           new int[]{ 1, 2, 3, 3, 5, -9}},
            /* empty */
            new Object[] { new int[][]{ },
                           0,
                           new int[]{ } },

            /* empty */
            new Object[] { new int[][]{ new int[] {},
                                           new int[] {}},
                           0,
                           new int[]{ } },
        };
    }

    @Test(dataProvider="arrays_int")
    void testMaxLength_int(int[][] ar, int columns, int[] flat) {
        assertEquals(ArrayUtil.maxLength(ar), columns);
    }

    @Test(dataProvider="arrays_int")
    void testFlatten_int(int[][] ar, int columns, int[] flat) {
        assertArrayEquals(ArrayUtil.flatten(ar, columns, -9), flat);
    }

    @DataProvider
    public Object[][] arrays_int3() {
        return new Object[][] {
            /* longer first */
            new Object[] { new int[][][] {new int[][]{ new int[]{1, 2, 3},
                                                       new int[]{3, 5}},
                                          new int[][]{ new int[]{1, 2, 3, 4},
                                                       new int[]{5}}},
                           4,
                           new int[]{ 1, 2, 3, -9,
                                      3, 5, -9, -9,
                                      1, 2, 3, 4,
                                      5, -9, -9, -9}},
            /* empty */
            new Object[] { new int[][][]{ },
                           0,
                           new int[]{ } },

            /* empty */
            new Object[] { new int[][][]{ new int[][]{ new int[] {},
                                                       new int[] {}}},
                           0,
                           new int[]{ } },
        };
    }

    @Test(dataProvider="arrays_int3")
    void testMaxLength_int3(int[][][] ar, int columns, int[] flat) {
        assertEquals(ArrayUtil.maxLength(ar), columns);
    }

    @Test(dataProvider="arrays_int3")
    void testFlatten_int3(int[][][] ar, int columns, int[] flat) {
        assertArrayEquals(ArrayUtil.flatten(ar, columns, -9), flat);
    }
}
