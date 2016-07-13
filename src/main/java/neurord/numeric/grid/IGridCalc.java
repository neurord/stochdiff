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

    int[][] getEventStatistics();

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
        /**
         * Index in the sequential numbering of all events
         */
        int event_number();
        /**
         * "source" voxel number
         */
        int element();
        /**
         * "target" voxel number (different from source only for diffusion)
         */
        int element2();

        String description();
        EventType event_type();

        /**
         * Indices of species on both sides of the reaction
         */
        int[] substrates();
        /**
         * Stoichiometries of species, negative on the lhs, positive on the rhs
         */
        int[] substrate_stoichiometry();

        int stat_index();
        String stat_index_description();

        Collection<Event> dependent();
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
