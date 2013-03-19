package org.textensor.stochdiff.numeric.grid;

import org.textensor.stochdiff.numeric.chem.StimulationTable;
import org.textensor.stochdiff.numeric.chem.ReactionTable;

public interface IGridCalc {
    int getGridPartNumb(int i, int outj);
    double getGridPartConc(int i, int outj);

    String[] getSpecieIDs();
    int[][] getSpecIndexesOut();
    String[] getRegionLabels();
    int[] getEltRegions();
    boolean[] getSubmembranes();
    String[] getRegionsOut();

    int[][] getReactionEvents();
    int[][][] getDiffusionEvents();

    StimulationTable getStimulationTable();
    int[][] getStimulationTargets();
    int[][] getStimulationEvents();

    ReactionTable getReactionTable();
}
