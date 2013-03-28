package org.textensor.stochdiff.numeric.grid;

import org.textensor.stochdiff.numeric.morph.VolumeGrid;

public interface ResultWriter {
    void init(String magic);
    void close();

    void writeGrid(VolumeGrid vgrid, double startTime, String fnmsOut[], IGridCalc source);
    void writeGridConcs(double time, int nel, int ispecout[], IGridCalc source);
    void writeGridConcsDumb(int i, double time, int nel, String fnamepart, IGridCalc source);

    void saveState(double time, String prefix, IGridCalc source);
    Object loadState(String initialStateFile, IGridCalc source);
}
