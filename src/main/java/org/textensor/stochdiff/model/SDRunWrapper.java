package org.textensor.stochdiff.model;

import org.textensor.stochdiff.disc.SpineLocator;
import org.textensor.stochdiff.disc.TreeBoxDiscretizer;
import org.textensor.stochdiff.disc.TreeCurvedElementDiscretizer;
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

    private VolumeGrid volumeGrid;

    private int[][] stimulationTargets;

    public final SDRun sdRun;

    public SDRunWrapper(SDRun sdRun) {
        this.sdRun = sdRun;

        extractGrid();
    }

    private void extractGrid() {
        final Morphology morph = sdRun.getMorphology();
        final TreePoint[] tpa = morph.getTreePoints();
        final Discretization disc = sdRun.getDiscretization();

        double d = disc.getDefaultMaxElementSide();

        // <--WK 6 22 2007
        // (1) iterate through all endpoints and their associated radii.
        // (2) divide each radius by successively increasing odd numbers until
        // the divided value becomes less than the defaultMaxElementSide.
        // (3) select the smallest among the divided radii values as d.
        double[] candidate_grid_sizes = new double[tpa.length];
        for (int i = 0; i < tpa.length; i++) {
            double diameter = tpa[i].r * 2;
            double denominator = 1;
            while (diameter / denominator > d)
                denominator += 2; // divide by successive odd numbers

            candidate_grid_sizes[i] = diameter / denominator;
        }

        d = Math.min(d, ArrayUtil.min(candidate_grid_sizes));
        log.info("subvolume grid size is: {} (from {}, {})",
                 d, disc.getDefaultMaxElementSide(), candidate_grid_sizes);

        final geometry_t vgg = sdRun.getGeometry();

        if (disc.curvedElements()) {
            TreeCurvedElementDiscretizer tced = new TreeCurvedElementDiscretizer(tpa);
            volumeGrid = tced.buildGrid(d, disc.getResolutionHM(), disc.getSurfaceLayers(), disc.getMaxAspectRatio());

        } else {
            TreeBoxDiscretizer tbd = new TreeBoxDiscretizer(tpa);
            volumeGrid = tbd.buildGrid(d, disc.getResolutionHM(), disc.getSurfaceLayers(),  vgg, sdRun.depth2D);
        }

        SpineLocator.locate(sdRun.spineSeed,
                            morph.getSpineDistribution(),
                            disc.getSpineDeltaX(),
                            volumeGrid);
        volumeGrid.fix();

        stimulationTargets =
            volumeGrid.getAreaIndexes(this.getStimulationTable().getTargetIDs());
    }

    public double[][] getRegionConcentrations() {
        String[] regions = this.volumeGrid.getRegionLabels();
        return this.sdRun.getInitialConditions().makeRegionConcentrations(regions, this.getSpecies());
    }

    public double[][] getRegionSurfaceDensities() {
        String[] regions = this.volumeGrid.getRegionLabels();
        return this.sdRun.getInitialConditions().makeRegionSurfaceDensities(regions, this.getSpecies());
    }

    public ReactionTable getReactionTable() {
        return this.sdRun.getReactionTable();
    }

    public StimulationTable getStimulationTable() {
        return this.sdRun.getStimulationTable();
    }

    public int[][] getStimulationTargets() {
        return this.stimulationTargets;
    }

    public VolumeGrid getVolumeGrid() {
        assert this.volumeGrid != null;
        return this.volumeGrid;
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
                        this.sdRun.runtime);
    }
}
