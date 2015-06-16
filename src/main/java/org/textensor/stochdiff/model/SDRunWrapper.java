package org.textensor.stochdiff.model;

import org.textensor.stochdiff.numeric.chem.ReactionTable;
import org.textensor.stochdiff.numeric.chem.StimulationTable;
import org.textensor.stochdiff.numeric.morph.VolumeGrid;
import org.textensor.stochdiff.numeric.morph.VolumeGrid.geometry_t;
import org.textensor.stochdiff.numeric.morph.TreePoint;
import org.textensor.util.ArrayUtil;
import org.textensor.util.inst;
import org.textensor.xml.ModelReader;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class SDRunWrapper {
    static final Logger log = LogManager.getLogger(SDRunWrapper.class);

    public final SDRun sdRun;

    public SDRunWrapper(SDRun sdRun) {
        this.sdRun = sdRun;
    }

    public double[][] getRegionConcentrations() {
        String[] regions = this.getVolumeGrid().getRegionLabels();
        return this.sdRun.getInitialConditions().makeRegionConcentrations(regions, this.getSpecies());
    }

    public double[][] getRegionSurfaceDensities() {
        String[] regions = this.getVolumeGrid().getRegionLabels();
        return this.sdRun.getInitialConditions().makeRegionSurfaceDensities(regions, this.getSpecies());
    }

    public ReactionTable getReactionTable() {
        return this.sdRun.getReactionTable();
    }

    public StimulationTable getStimulationTable() {
        return this.sdRun.getStimulationTable();
    }

    public int[][] getStimulationTargets() {
        return this.sdRun.getStimulationTargets();
    }

    public VolumeGrid getVolumeGrid() {
        return this.sdRun.getVolumeGrid();
    }

    public String[] getSpecies() {
        return this.sdRun.getSpecies();
    }

    public String serialize() {
        try {
            ModelReader<SDRun> loader = new ModelReader(SDRun.class);
            return loader.marshall(this.sdRun);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public double stepSize() {
        return Math.min(Math.min(this.sdRun.getFixedStepDt(),
                                 this.sdRun.getOutputInterval()),
                        this.sdRun.getEndTime() - this.sdRun.getStartTime());
    }
}
