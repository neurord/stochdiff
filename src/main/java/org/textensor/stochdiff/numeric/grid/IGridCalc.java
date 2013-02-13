package org.textensor.stochdiff.numeric.grid;

public interface IGridCalc {
    int getGridPartNumb(int i, int outj);
    double getGridPartConc(int i, int outj);

    String[] getSpecieIDs();
    int[][] getSpecIndexesOut();
    String[] getRegionLabels();
    int[] getEltRegions();
    boolean[] getSubmembranes();
    String[] getRegionsOut();
}
