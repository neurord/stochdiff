package org.textensor.stochdiff.model;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.StringTokenizer;

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

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class SDRunWrapper {
    static final Logger log = LogManager.getLogger(SDRunWrapper.class);

    public ReactionTable reactionTable;
    public VolumeGrid volumeGrid;
    public StimulationTable stimulationTable;

    public double[] baseConcentrations;

    public double[][] regionConcentrations;
    public double[][] regionSurfaceDensities;

    String[] speciesList;

    // indices of output species
    private final int[] ispecout;

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

        this.ispecout = findOutputSpecies(sdRun.outputSpecies,
                                          reactionTable.getSpecieIDs());
    }

    private void extractTables() {
        reactionTable = sdRun.getReactionScheme().makeReactionTable();

        stimulationTable = sdRun.getStimulationSet().makeStimulationTable(reactionTable);

        speciesList = reactionTable.getSpecieIDs();

        baseConcentrations = sdRun.getInitialConditions()
                                  .getDefaultNanoMolarConcentrations(speciesList);

        /*
         * RCC - not sure restricting the output regions is useful for the ccviz
         * file? String regout = sdRun.outputRegions; if (regout == null ||
         * regout.equals("all")) { iregout = null;
         *
         * } else if (regout.length() == 0 || regout.equals("none")) { iregout =
         * new int[0];
         *
         * } else { iregout = getIndices(regout, speciesList); }
         */

        extractOutputScheme(reactionTable);
    }

    private static int[] findOutputSpecies(String specout, String[] species) {
        if (specout == null || specout.equals("all"))
            return ArrayUtil.iota(species.length);
        else if (specout.length() == 0 || specout.equals("none"))
            return new int[0];
        else
            return getIndices(specout, species);
    }

    private static int[] getIndices(String matchString, String[] species) {
        HashMap<String, Integer> isdhm = inst.newHashMap();
        for (int i = 0; i < species.length; i++)
            isdhm.put(species[i], i);

        StringTokenizer st = new StringTokenizer(matchString, " ,");
        ArrayList<Integer> wk = new ArrayList<Integer>();
        while (st.hasMoreTokens()) {
            String so = st.nextToken();
            if (isdhm.containsKey(so)) {
                wk.add(isdhm.get(so));
            } else {
                log.error("Unknown output species '{}' " +
                          "(requested or output but not in reaction scheme)", so);
                throw new RuntimeException("unknown species '" + so + "'");
            }
        }
        int[] ret = new int[wk.size()];
        for (int i = 0; i < wk.size(); i++)
            ret[i] = wk.get(i);

        return ret;
    }

    private void extractOutputScheme(ReactionTable rtab) {
        OutputScheme os = sdRun.getOutputScheme();

        int nos = os.outputSets.size();
        regionsOut = new String[nos];
        dtsOut = new double[nos];
        fnmsOut = new String[nos];
        specNamesOut = new String[nos][];
        specIndexesOut = new int[nos][];

        String[] specieIDs = rtab.getSpecieIDs();
        int nspec = specieIDs.length;

        log.info("extracting output scheme with {} file for {} species", os.outputSets.size(), nspec);


        for (int i = 0; i < os.outputSets.size(); i++) {
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
                    if (specNamesOut[i][k].equalsIgnoreCase(specieIDs[kq]))
                        specIndexesOut[i][k] = kq;
        }
    }

    private void extractGrid() {
        Morphology morph = sdRun.getMorphology();
        TreePoint[] tpa = morph.getTreePoints();
        Discretization disc = sdRun.getDiscretization();

        double d = disc.defaultMaxElementSide;

        // <--WK 6 22 2007
        // (1) iterate through all endpoints and their associated radii.
        // (2) divide each radius by successively increasing odd numbers until
        // the divided value becomes less than the defaultMaxElementSide.
        // (3) select the smallest among the divided radii values as d.
        double[] candidate_grid_sizes = new double[tpa.length];
        for (int i = 0; i < tpa.length; i++) {
            double diameter = tpa[i].r * 2.0;
            double denominator = 1.0;
            while (diameter / denominator > d)
                denominator += 2.0; // divide by successive odd numbers

            candidate_grid_sizes[i] = diameter / denominator;
        }

        d = Math.min(d, ArrayUtil.min(candidate_grid_sizes));
        log.info("subvolume grid size is: {}", d);

        final geometry_t vgg = geometry_t.fromString(sdRun.geometry);
        final double d2d = sdRun.depth2D > 0 ? sdRun.depth2D : 0.5;

        if (disc.curvedElements()) {
            TreeCurvedElementDiscretizer tced = new TreeCurvedElementDiscretizer(tpa);
            volumeGrid = tced.buildGrid(d, disc.getResolutionHM(), disc.getSurfaceLayers(), disc.getMaxAspectRatio());

        } else {
            TreeBoxDiscretizer tbd = new TreeBoxDiscretizer(tpa);
            volumeGrid = tbd.buildGrid(d, disc.getResolutionHM(), disc.getSurfaceLayers(),  vgg, d2d);
        }

        SpineLocator.locate(sdRun.spineSeed, morph.getSpineDistribution(), disc.spineDeltaX,
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
        for (int i = 0; i < sra.length; i++) {
            // RCC now we get the base concentrations everywhere, and just
            // override
            // those values that are explicitly set elsewhere
            ret[i] = new double[baseConcentrations.length];
            for (int j = 0; j < nc; j++)
                ret[i][j] = baseConcentrations[j];

            if (icons.hasConcentrationsFor(sra[i])) {
                double[] wk = icons.getRegionConcentrations(sra[i], speciesList);
                for (int j = 0; j < nc; j++)
                    if (wk[j] >= 0.)
                        ret[i][j] = wk[j];
            }
        }
        return ret;
    }

    private double[][] makeRegionSurfaceDensities(String[] sra) {
        InitialConditions icons = sdRun.getInitialConditions();

        double[][] ret = new double[sra.length][];
        for (int i = 0; i < sra.length; i++)
            if (icons.hasSurfaceDensitiesFor(sra[i]))
                ret[i] = icons.getRegionSurfaceDensities(sra[i], speciesList);

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

    // XXX: rename this
    public String[] getSpecieIDs() {
        return this.reactionTable.getSpecieIDs();
    }

    public int[] getOutputSpecies() {
        assert this.ispecout != null;
        return this.ispecout;
    }
}
