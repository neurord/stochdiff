package neurord.numeric.grid;

import java.util.Collection;

import neurord.numeric.chem.StimulationTable;
import neurord.numeric.chem.ReactionTable;
import neurord.numeric.morph.VolumeGrid;
import neurord.model.SDRun;

public interface IGridCalc {
    /*
     * An identifier of a trial. Should start at 0.
     */
    int trial();

    int getGridPartNumb(int i, int outj);
    double getGridPartConc(int i, int outj);

    boolean preferConcs();

    SDRun getSource();
    long getSimulationSeed();

    int getNumberElements();

    int[][] getReactionEvents();
    int[][][] getDiffusionEvents();
    int[][] getStimulationEvents();

    public enum EventType {
        REACTION,
        DIFFUSION,
        STIMULATION,
    }

    public enum HappeningKind {
        EXACT,
        LEAP,
    }

    public interface Event {
        int event_number();
        int element();
        String description();
        EventType event_type();
        Collection<Event> dependent();
        long firings();
    }

    public interface Happening {
        int event_number();
        HappeningKind kind();
        int extent();
        double time();
        double waited();
        double original_wait();
    }

    Collection<Event> getEvents();
    Collection<Happening> getHappenings();
}
