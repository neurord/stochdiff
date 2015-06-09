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

    private ReactionTable reactionTable;
    private VolumeGrid volumeGrid;
    private StimulationTable stimulationTable;

    public double[] baseConcentrations;

    public double[][] regionConcentrations;
    public double[][] regionSurfaceDensities;

    public int[][] specIndexesOut;
    public String[] regionsOut;
    public double[] dtsOut;
    public String[] fnmsOut;
    public String[][] specNamesOut;

    private int[][] stimulationTargets;

    public final SDRun sdRun;

    public SDRunWrapper(SDRun sdRun) {
        this.sdRun = sdRun;

        extractGrid();
    }

    private void extractTables() {
        ReactionScheme rs = sdRun.getReactionScheme();
        reactionTable = rs.makeReactionTable();

        stimulationTable = sdRun.getStimulationSet().makeStimulationTable(reactionTable);

        baseConcentrations = sdRun.getInitialConditions()
                                  .getDefaultNanoMolarConcentrations(rs.getSpecies());

        /*
         * RCC - not sure restricting the output regions is useful for the ccviz
         * file?
         */

        extractOutputScheme(reactionTable);
    }

    private void extractOutputScheme(ReactionTable rtab) {
        OutputScheme os = sdRun.getOutputScheme();

        int nos = os != null ? os.outputSets.size() : 0;
        regionsOut = new String[nos];
        dtsOut = new double[nos];
        fnmsOut = new String[nos];
        specNamesOut = new String[nos][];
        specIndexesOut = new int[nos][];

        String[] species = rtab.getSpecies();
        int nspec = species.length;

        log.info("extracting output scheme with {} files for {} species", nos, nspec);

        for (int i = 0; i < nos; i++) {
            OutputSet oset = os.outputSets.get(i);

            if (oset.hasdt())
                dtsOut[i] = oset.getdt();
            else
                dtsOut[i] += sdRun.fixedStepDt;

            fnmsOut[i] = oset.getFname();
            specNamesOut[i] = oset.getNamesOfOutputSpecies();

            if (oset.hasRegion())
                regionsOut[i] = oset.getRegion();
            else
                regionsOut[i] = "default"; // RC uses "default" as default value

            specIndexesOut[i] = new int[specNamesOut[i].length];

            for (int k = 0; k < specNamesOut[i].length; k++)
                for (int kq = 0; kq < nspec; kq++)
                    if (specNamesOut[i][k].equalsIgnoreCase(species[kq]))
                        specIndexesOut[i][k] = kq;
        }
    }

    private void extractGrid() {
        final Morphology morph = sdRun.getMorphology();
        final TreePoint[] tpa = morph.getTreePoints();
        Discretization disc = sdRun.getDiscretization();
        if (disc == null)
            disc = Discretization.SINGLE_VOXEL;

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

        final geometry_t vgg = geometry_t.fromString(sdRun.geometry);

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

        extractTables();

        regionConcentrations = makeRegionConcentrations(volumeGrid.getRegionLabels());
        regionSurfaceDensities = makeRegionSurfaceDensities(volumeGrid.getRegionLabels());

        stimulationTargets =
            volumeGrid.getAreaIndexes(this.stimulationTable.getTargetIDs());

    }

    private double[][] makeRegionConcentrations(String[] sra) {
        InitialConditions icons = sdRun.getInitialConditions();
        int nc = baseConcentrations.length;
        double[][] ret = new double[sra.length][];
        String[] species = this.sdRun.getReactionScheme().getSpecies();
        for (int i = 0; i < sra.length; i++) {
            // RCC now we get the base concentrations everywhere, and just
            // override
            // those values that are explicitly set elsewhere
            ret[i] = new double[baseConcentrations.length];
            for (int j = 0; j < nc; j++)
                ret[i][j] = baseConcentrations[j];

            if (icons.hasConcentrationsFor(sra[i])) {
                double[] wk = icons.getRegionConcentrations(sra[i], species);
                for (int j = 0; j < nc; j++)
                    if (wk[j] >= 0.)
                        ret[i][j] = wk[j];
            }
        }
        return ret;
    }

    private double[][] makeRegionSurfaceDensities(String[] sra) {
        InitialConditions icons = sdRun.getInitialConditions();
        String[] species = this.sdRun.getReactionScheme().getSpecies();

        double[][] ret = new double[sra.length][];
        for (int i = 0; i < sra.length; i++)
            if (icons.hasSurfaceDensitiesFor(sra[i]))
                ret[i] = icons.getRegionSurfaceDensities(sra[i], species);

        return ret;
    }

    public double[] getNanoMolarConcentrations() {
        assert this.baseConcentrations != null;
        return this.baseConcentrations;
    }

    public double[][] getRegionConcentrations() {
        assert this.regionConcentrations != null;
        return this.regionConcentrations;
    }

    public double[][] getRegionSurfaceDensities() {
        assert this.regionSurfaceDensities != null;
        return this.regionSurfaceDensities;
    }

    public ReactionTable getReactionTable() {
        assert this.reactionTable != null;
        return this.reactionTable;
    }

    public StimulationTable getStimulationTable() {
        assert this.stimulationTable != null;
        return this.stimulationTable;
    }

    public int[][] getStimulationTargets() {
        return this.stimulationTargets;
    }

    public VolumeGrid getVolumeGrid() {
        assert this.volumeGrid != null;
        return this.volumeGrid;
    }

    public int[][] getSpecIndexesOut() {
        assert this.specIndexesOut != null;
        return this.specIndexesOut;
    }

    public String[] getRegionsOut() {
        assert this.specIndexesOut != null;
        return this.regionsOut;
    }

    public String[] getSpecies() {
        return this.reactionTable.getSpecies();
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
        return Math.min(Math.min(this.sdRun.fixedStepDt,
                                 this.sdRun.getOutputInterval()),
                        this.sdRun.runtime);
    }
}
