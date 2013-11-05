package org.textensor.stochdiff.numeric.grid;

import java.util.Collection;

import org.textensor.stochdiff.numeric.chem.StimulationTable;
import org.textensor.stochdiff.numeric.chem.ReactionTable;
import org.textensor.stochdiff.numeric.morph.VolumeGrid;
import org.textensor.stochdiff.model.SDRunWrapper;

public interface IGridCalc {
    /*
     * An identifier of a trial. Should start at 0.
     */
    int trial();

    int getGridPartNumb(int i, int outj);
    double getGridPartConc(int i, int outj);

    boolean preferConcs();

    SDRunWrapper getSource();
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

    public enum EventKind {
        EXACT,
        LEAP,
    }

    public interface Event {
        int index();
        EventType type();
        EventKind kind();
        int extent();
        double time();
        double waited();
    }

    Collection<Event> getEvents();
}
