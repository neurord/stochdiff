package org.textensor.stochdiff.numeric.grid;

import java.io.File;

import org.textensor.stochdiff.numeric.morph.VolumeGrid;

/* ResultWriterText supports two different text formats. One is used to write
 * .out file at <outputInterval>, the other is used to write .conc.txt.out files
 * at the intervals specified in <outputSet>s.  In the first case,
 * <outputSpecies> specifies which species should be written, in the latter
 * dt attribute of <OutputSet>.
 */
public interface ResultWriter {
    void init(String magic);
    void close();

    File outputFile();

    void writeGrid(VolumeGrid vgrid, double startTime, String fnmsOut[], IGridCalc source);

    /* Write the output specified in the first way. */
    void writeOutputInterval(double time, int nel, int ispecout[], IGridCalc source);

    /* Write the output specified in the second way. */
    void writeOutputScheme(int i, double time, int nel, String fnamepart, IGridCalc source);

    void saveState(double time, String prefix, IGridCalc source);
    Object loadState(String initialStateFile, IGridCalc source);

    void closeTrial(IGridCalc source);
}
