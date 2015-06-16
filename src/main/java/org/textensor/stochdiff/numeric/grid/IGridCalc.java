package org.textensor.stochdiff.numeric.grid;

import java.util.Collection;

import org.textensor.stochdiff.numeric.chem.StimulationTable;
import org.textensor.stochdiff.numeric.chem.ReactionTable;
import org.textensor.stochdiff.numeric.morph.VolumeGrid;
import org.textensor.stochdiff.model.SDRun;

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
    }

    public interface Happening {
        int event_number();
        HappeningKind kind();
        int extent();
        double time();
        double waited();
    }

    Collection<Event> getEvents();
    Collection<Happening> getHappenings();
}
