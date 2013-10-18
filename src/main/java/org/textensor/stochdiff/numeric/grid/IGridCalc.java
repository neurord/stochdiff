package org.textensor.stochdiff.numeric.grid;

import java.util.Collection;

import org.textensor.stochdiff.numeric.chem.StimulationTable;
import org.textensor.stochdiff.numeric.chem.ReactionTable;
import org.textensor.stochdiff.numeric.morph.VolumeGrid;

public interface IGridCalc {
    int getGridPartNumb(int i, int outj);
    double getGridPartConc(int i, int outj);

    boolean preferConcs();

    String[] getSpecieIDs();
    int[][] getSpecIndexesOut();
    int[] getEltRegions();
    String[] getRegionsOut();

    VolumeGrid getVolumeGrid();

    int getNumberElements();

    int[][] getReactionEvents();

    int[][][] getDiffusionEvents();

    StimulationTable getStimulationTable();
    int[][] getStimulationTargets();
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

    ReactionTable getReactionTable();
}
