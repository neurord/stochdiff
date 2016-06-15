package neurord.numeric.grid;

import java.util.ArrayList;
import java.util.Arrays;

import neurord.model.InjectionStim;
import neurord.numeric.math.RandomGenerator;
import neurord.numeric.chem.ReactionTable;
import neurord.numeric.chem.StimulationTable;
import neurord.numeric.chem.StimulationTable.Stimulation;
import neurord.numeric.morph.VolumeGrid;
import neurord.numeric.morph.VolumeElement;
import neurord.numeric.morph.CuboidVolumeElement;
import neurord.geom.Position;
import neurord.geom.GPosition;

import static org.testng.Assert.assertEquals;
import static neurord.util.TestUtil.assertArrayEquals;
import static neurord.util.TestUtil.assertApproxEquals;
import org.testng.annotations.*;

public class TestStimulation {
    public final String SPECIES = "A";
    public final String SITE = "label";
    public final double RATE = 2;
    public final double ONSET = 100;
    public final double DURATION = 10;
    public final double PERIOD = 30;
    public final double END = 200;

    final ReactionTable rtab = new ReactionTable(0,
                                                 new String[]{ SPECIES },
                                                 new double[]{ });

    final InjectionStim stim = new InjectionStim(SPECIES, SITE, RATE, ONSET, DURATION, PERIOD, END);
    final StimulationTable stimtab = new StimulationTable(Arrays.asList(stim), rtab);
    final VolumeGrid grid = new VolumeGrid();
    {
        VolumeElement el = new CuboidVolumeElement("label", "region", "groupID",
                                                   new Position[]{},
                                                   new Position[]{},
                                                   1.0,
                                                   new GPosition(0.1, 0.2, 0.3),
                                                   2.0, 3.0, 4.0, 5.0, 0.5);
        grid.addElement(el);
    }

    static class FakeRandom implements RandomGenerator {
        @Override public float random() { return 0.5f; }
        @Override public double gaussian() { return 0; }
        @Override public double gammln(double xx) { return xx; }
        @Override public int poisson(double mean) { return (int) Math.ceil(mean); }
        @Override public int round(double mean) { return (int) Math.floor(mean); }
        @Override public double exponential(double tau) { return 1/tau; }
        @Override public long used() { return 0; }
    }

    NextEventQueue queue = new NextEventQueue(new FakeRandom(), null, new int[1][1], true, 0.1, 1);
    NextEventQueue.Numbering numbering = new NextEventQueue.Numbering();
    ArrayList<NextEventQueue.NextStimulation> stims =
        queue.createStimulations(numbering, grid, rtab, stimtab, "none", null);

    @Test
    public void testStim() {
        Stimulation stim = stimtab.getStimulations().get(0);
        assertEquals(stim.site, SITE);
        assertEquals(stim.rate, RATE);
        assertEquals(stim.onset, ONSET);
        assertEquals(stim.duration, DURATION);
        assertEquals(stim.period, PERIOD);
        assertEquals(stim.end, END);

        assertEquals(stims.size(), 1);

        NextEventQueue.NextStimulation ev = stims.get(0);
        assertEquals(ev.calcPropensity(), RATE);

        assertEquals(ev.time(), stim.onset + 1/RATE);

        // Check that time is properly counted from the onset,
        // execting: onset + exp(tau) = onset + tau
        assertEquals(ev._new_time(0), stim.onset + 1/RATE);

        // Check that when current < onset, time does not change
        assertEquals(ev._new_time(50), stim.onset + 1/RATE);
        assertEquals(ev._new_time(100), stim.onset + 1/RATE);

        // Check that when inside first window, time moves to the right
        assertEquals(ev._new_time(100 + 5), stim.onset + 1/RATE + 5);

        // Check that when before second window, time is stuck
        assertEquals(ev._new_time(100 + stim.duration), stim.onset + stim.period + 1/RATE);

        // Check that when inside second window, time moves to the right
        assertEquals(ev._new_time(100 + stim.period), stim.onset + stim.period + 1/RATE);
        assertEquals(ev._new_time(100 + stim.period + 5),
                     stim.onset + stim.period + 1/RATE + 5);

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
