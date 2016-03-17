package org.textensor.stochdiff.numeric.stochastic;

import java.util.Random;
import java.util.ArrayList;

import org.textensor.stochdiff.numeric.math.MersenneTwister;
import org.textensor.stochdiff.numeric.BaseCalc.distribution_t;

import static org.testng.Assert.assertEquals;
import static org.textensor.util.TestUtil.assertArrayEquals;
import static org.textensor.util.TestUtil.assertApproxEquals;
import org.testng.annotations.*;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class TestTables {
    static final Logger log = LogManager.getLogger(TestTables.class);

    @DataProvider
    public Object[][] tables() {
        ArrayList<Object[]> t = new ArrayList<>();
        for (int i = 10; i <= 90; i += 10)
            for (int ip = -6; ip < -1; ip++)
                for (distribution_t mode: distribution_t.values())
                    t.add(new Object[] {new NGoTable(i, 2 * ip, mode)});

        for (int i = 1; i <= 100; i+= 10)
            for (distribution_t mode: distribution_t.values()) {
                int n = 120 + i;
                if (n <= BinomialTable.NMAX)
                    t.add(new Object[] {new NGoTable(n, Math.log(i/100.), mode)});
            };

        return t.toArray(new Object[0][]);
    }

    @Test(dataProvider="tables")
    public void nGo_binary_search(NGoTable jc) {
        for (int i = 0; i <= 50; i++) {
            double r = i / 50.;
            int n1 = jc.nGo(r);
            int n2 = jc.nGoBS(r);
            assertApproxEquals(n1, n2, 0.01, 1, "i=" + i + " r=" + r + " jc=" + jc);
        }
    }

    @DataProvider
    public static Object[][] modes() {
        ArrayList<Object[]> t = new ArrayList<>();
        for (distribution_t mode: distribution_t.values())
            t.add(new Object[] {mode});
        return t.toArray(new Object[0][]);
    }

    @Test(dataProvider="modes")
    public void nGo_zero_probability(distribution_t mode) {
        NGoTable t = new NGoTable(120, Double.NEGATIVE_INFINITY, mode);
        assertEquals(t.nGo(0.5), 0);
    }
}
