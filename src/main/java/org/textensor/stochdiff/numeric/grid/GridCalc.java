package org.textensor.stochdiff.numeric.grid;

import org.textensor.stochdiff.model.SDRun;
import org.textensor.stochdiff.numeric.BaseCalc;
import org.textensor.stochdiff.numeric.morph.VolumeGrid;
import org.textensor.stochdiff.numeric.math.Column;
import org.textensor.stochdiff.numeric.chem.ReactionTable;
import org.textensor.stochdiff.numeric.chem.StimulationTable;
import static org.textensor.stochdiff.numeric.grid.ResultWriterText.stringd;

public abstract class GridCalc extends BaseCalc implements IGridCalc {
    Column mconc;

    ReactionTable rtab;
    public VolumeGrid vgrid;

    StimulationTable stimTab;

    double dt;

    public int nel;
    int nspec;
    public String[] specieIDs;

    double[] volumes;
    double[] lnvolumes;
    double[] fdiff;

    public boolean[] submembranes;
    public String[] regionLabels;

    int[][] neighbors;
    double[][] couplingConstants;

    public int[] eltregions;
    int[][] stimtargets;

    double[] surfaceAreas;

    double stateSaveTime;

    protected GridCalc(SDRun sdm) {
        super(sdm);
    }

    protected void init() {
        stateSaveTime = sdRun.getStateSaveInterval();
        if (stateSaveTime <= 0.0) {
            stateSaveTime = 1.e9;
        }

        rtab = getReactionTable();
        specieIDs = rtab.getSpecieIDs();

        vgrid = getVolumeGrid();
        nel = vgrid.getNElements();

        // WK 6 18 2007
        submembranes = vgrid.getSubmembranes();
        regionLabels = vgrid.getRegionLabels();
        // WK

        eltregions = vgrid.getRegionIndexes();
    }

    @SuppressWarnings("boxing")
    protected String getStateText() {
        StringBuffer sb = new StringBuffer();
        sb.append("nrds " + nel + " " + specieIDs.length + "\n");
        for (int i = 0; i < specieIDs.length; i++) {
            sb.append(specieIDs[i] + " ");
        }
        sb.append("\n");
        for (int i = 0; i < nel; i++) {
            for (int j = 0; j < specieIDs.length; j++) {
                sb.append(stringd(this.getGridPartConc(i, j)));
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    @Override
    public String[] getSpecieIDs() {
        return this.specieIDs;
    }

    @Override
    public String[] getRegionLabels() {
        return this.regionLabels;
    }

    @Override
    public int[] getEltRegions() {
        return this.eltregions;
    }

    @Override
    public boolean[] getSubmembranes() {
        return this.submembranes;
    }
}
