package org.textensor.stochdiff.numeric.grid;

import org.textensor.stochdiff.numeric.BaseCalc;

public interface GridCalc {
    int getGridPartNumb(int i, int outj);
    double getGridPartConc(int i, int outj);

    String[] getSpecieIDs();
    int[][] getSpecIndexesOut();
    String[] getRegionLabels();
    int[] getEltRegions();
    boolean[] getSubmembranes();
    String[] getRegionsOut();
}
