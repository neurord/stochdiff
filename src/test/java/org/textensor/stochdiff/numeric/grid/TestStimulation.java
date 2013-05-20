package org.textensor.stochdiff.numeric.grid;

import java.util.ArrayList;

import org.textensor.stochdiff.numeric.math.RandomGenerator;
import org.textensor.stochdiff.numeric.chem.StimulationTable;
import org.textensor.stochdiff.numeric.chem.StimulationTable.Stimulation;

import static org.testng.Assert.assertEquals;
import static org.textensor.util.TestUtil.assertArrayEquals;
import static org.textensor.util.TestUtil.assertApproxEquals;
import org.testng.annotations.*;

public class TestStimulation {
    final double[] rates = {2.};
    public final double END = 200;
    StimulationTable stimtab = new StimulationTable();
    {
        stimtab.addPeriodicSquarePulse("xxx", rates,
                                       100., 10., 30., END);
    }

    static class FakeRandom implements RandomGenerator {
        @Override public float random() { return 0.5f; }
        @Override public double gaussian() { return 0; }
        @Override public double gammln(double xx) { return xx; }
        @Override public int poisson(double mean) { return (int) Math.ceil(mean); }
        @Override public double exponential(double tau) { return 1/tau; }
    }

    NextEventQueue queue = new NextEventQueue(new FakeRandom(), new int[1][1]);
    ArrayList<NextEventQueue.NextStimulation> stims =
        queue.createStimulations(null, stimtab, new int[][]{{0}});

    @Test
    public void testStim() {
        Stimulation stim = stimtab.getStimulations().get(0);
        assertEquals(stim.site, "xxx");
        assertEquals(stim.rates, rates);
        assertEquals(stim.onset, 100.);
        assertEquals(stim.duration, 10.);
        assertEquals(stim.period, 30.);
        assertEquals(stim.end, END);

        assertEquals(stims.size(), 1);

        NextEventQueue.NextStimulation ev = stims.get(0);
        assertEquals(ev._propensity(), rates[0]);

        assertEquals(ev.time(), stim.onset + 1/rates[0]);

        // Check that time is properly counted from the onset,
        // execting: onset + exp(tau) = onset + tau
        assertEquals(ev._new_time(0), stim.onset + 1/rates[0]);

        // Check that when current < onset, time does not change
        assertEquals(ev._new_time(50), stim.onset + 1/rates[0]);
        assertEquals(ev._new_time(100), stim.onset + 1/rates[0]);

        // Check that when inside first window, time moves to the right
        assertEquals(ev._new_time(100 + 5), stim.onset + 1/rates[0] + 5);

        // Check that when before second window, time is stuck
        assertEquals(ev._new_time(100 + stim.duration), stim.onset + stim.period + 1/rates[0]);

        // Check that when inside second window, time moves to the right
        assertEquals(ev._new_time(100 + stim.period), stim.onset + stim.period + 1/rates[0]);
        assertEquals(ev._new_time(100 + stim.period + 5),
                     stim.onset + stim.period + 1/rates[0] + 5);

        // Check that when after end, time is infinity
        assertEquals(ev._new_time(END), Double.POSITIVE_INFINITY);
        assertEquals(ev._new_time(END + 10), Double.POSITIVE_INFINITY);
    }

    public static void main(String... args) {
        TestStimulation test = new TestStimulation();
        NextEventQueue.NextStimulation ev = test.stims.get(0);
        for (double t=0; t < test.END + 100; t+=0.05)
            System.out.println(String.format("%f %f", t, ev._new_time(t)));
    }
}
