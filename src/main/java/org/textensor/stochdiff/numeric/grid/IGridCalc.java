package org.textensor.stochdiff.numeric.grid;

import org.textensor.stochdiff.numeric.chem.StimulationTable;
import org.textensor.stochdiff.numeric.chem.ReactionTable;

public interface IGridCalc {
    int getGridPartNumb(int i, int outj);
    double getGridPartConc(int i, int outj);

    boolean preferConcs();

    String[] getSpecieIDs();
    int[][] getSpecIndexesOut();
    String[] getRegionLabels();
    int[] getEltRegions();
    boolean[] getSubmembranes();
    String[] getRegionsOut();

    int getNumberElements();

    int[][] getReactionEvents();
    int[][][] getDiffusionEvents();

    StimulationTable getStimulationTable();
    int[][] getStimulationTargets();
    int[][] getStimulationEvents();

    ReactionTable getReactionTable();
}
