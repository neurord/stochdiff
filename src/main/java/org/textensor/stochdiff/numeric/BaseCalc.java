//3 5 2008: WK changed the initial value of the denominator variable in the extractGrid function from 3 to 1
//6 22 2007: WK modified the extractGrid() function to calculate the side-length of
//           each volume element (which is a square with a predefined thickness).
//6 19 2007: WK added 1 variable and 1 function to be able to output by user-specified 'region's.
//5 16 2007: WK added 4 variables and 5 functions (within <--WK ... WK-->)
//written by Robert Cannon
package org.textensor.stochdiff.numeric;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.textensor.report.E;
import org.textensor.stochdiff.ResultWriter;
import org.textensor.stochdiff.disc.SpineLocator;
import org.textensor.stochdiff.disc.TreeBoxDiscretizer;
import org.textensor.stochdiff.disc.TreeCurvedElementDiscretizer;
import org.textensor.stochdiff.inter.SDState;
import org.textensor.stochdiff.inter.StateReader;
import org.textensor.stochdiff.model.*;
import org.textensor.stochdiff.numeric.chem.ReactionTable;
import org.textensor.stochdiff.numeric.chem.StimulationTable;
import org.textensor.stochdiff.numeric.morph.TreePoint;
import org.textensor.stochdiff.numeric.morph.VolumeGrid;
import org.textensor.util.ArrayUtil;

public abstract class BaseCalc {

    public SDRun sdRun;

    ReactionTable reactionTable;
    VolumeGrid volumeGrid;
    StimulationTable stimulationTable;

    double[] baseConcentrations;

    double[][] regionConcentrations;
    double[][] regionSurfaceDensities;

    protected ResultWriter resultWriter;

    String[] speciesList;

    // indices of output species
    public int[] ispecout;

    public enum distribution_t {
        BINOMIAL,
        POISSON,
    }

    public static final int INDEPENDENT = 0;
    public static final int SHARED = 1;
    public static final int PARTICLE = 2;

    distribution_t distID = distribution_t.BINOMIAL;
    protected int algoID = INDEPENDENT;

    public boolean writeConcentration = false;

    // <--WK
    public String n_list, dt_list, filename_list, specie_names_list;
    // 6 18 2007
    public String region_list;
    // WK-->


    public final static int VISUALIZE = 1;
    public final static int RUN = 0;

    public int runAction = 0;


    protected int[][] specIndexesOut;
    protected String[] regionsOut;
    protected double[] dtsOut;
    protected String[] fnmsOut;
    protected String[][] specNamesOut;

    public BaseCalc(SDRun sdr) {
        sdRun = sdr;

        if (sdr.action == null) {

        } else if (sdr.action.startsWith("vis")) {
            runAction = VISUALIZE;
        } else if (sdr.action.startsWith("run")) {
            runAction = RUN;

        } else {
            E.error("Unrecognized action: only 'visualize' is supported");
        }

    }





    protected String stringd(double d) {
        if (d == 0.0) {
            return "0.0 ";
        } else {
            return String.format("%.5g ", d);
        }
    }

    // <--RO 7 02 2008
    // Saves as integers; used to save particles instead of concentrations
    protected String stringi(int id) {
        if (id == 0) {
            return "00 ";
        } else {
            return String.format("%d ", id);
        }
    }

    public void extractTables() {
        distID = sdRun.getDistributionID();
        algoID = sdRun.getAlgorithmID();

        ReactionScheme rsch = sdRun.getReactionScheme();

        reactionTable = rsch.makeReactionTable();

        StimulationSet stim = sdRun.getStimulationSet();
        stimulationTable = stim.makeStimulationTable(reactionTable);

        InitialConditions icons = sdRun.getInitialConditions();
        speciesList = reactionTable.getSpeciesIDs();
        // -------------------------
        // OutputScheme output = sdRun.getOutputScheme();
        // -------------------------
        // double vol = sdRun.poolVolume;
        baseConcentrations = icons.getDefaultNanoMolarConcentrations(speciesList);

        String specout = sdRun.outputSpecies;
        if (specout == null || specout.equals("all")) {
            ispecout = new int[speciesList.length];
            for (int i = 0; i < speciesList.length; i++) {
                ispecout[i] = i;
            }

        } else if (specout.length() == 0 || specout.equals("none")) {
            ispecout = new int[0];

        } else {
            ispecout = getIndices(specout, speciesList);
        }
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

        String oq = sdRun.outputQuantity;
        if (oq != null) {
            if (oq.equals("NUMBER")) {
                writeConcentration = false;
                E.info("Output will contain particle numbers");

            } else if (oq.equals("CONCENTRATION")) {
                writeConcentration = true;
                E.info("Output will contain concentrations");

            } else {

                E.warning("Unrecognized output quantity: " + oq + " - need either NUMBER or CONCENTRATION");
            }
        }
    }

    public int[] getIndices(String matchString, String[] idlist) {
        HashMap<String, Integer> isdhm = new HashMap<String, Integer>();
        for (int i = 0; i < idlist.length; i++) {
            isdhm.put(idlist[i], i);
        }
        StringTokenizer st = new StringTokenizer(matchString, " ,");
        ArrayList<Integer> wk = new ArrayList<Integer>();
        while (st.hasMoreTokens()) {
            String so = st.nextToken();
            if (isdhm.containsKey(so)) {
                wk.add(isdhm.get(so));
            } else {
                E.warning("Unknown output species " + so + " (requested or output but not in reaction scheme)");
            }
        }
        int[] ret = new int[wk.size()];
        for (int i = 0; i < wk.size(); i++) {
            ret[i] = wk.get(i);
        }
        return ret;
    }





    public void extractOutputScheme(ReactionTable rtab) {
        OutputScheme os = sdRun.getOutputScheme();

        int nos = os.outputSets.size();
        regionsOut = new String[nos];
        dtsOut = new double[nos];
        fnmsOut = new String[nos];
        specNamesOut = new String[nos][];
        specIndexesOut = new int[nos][];

        String[] specieIDs = rtab.getSpeciesIDs();
        int nspec = specieIDs.length;

        E.info("extracting output scheme " + os.outputSets.size() + " " + nspec);


        for (int i = 0; i < os.outputSets.size(); i++) {
            OutputSet oset = (os.outputSets).get(i);

            if (oset.hasdt()) {
                dtsOut[i] = oset.getdt();
            } else {
                dtsOut[i] += sdRun.fixedStepDt;
            }
            fnmsOut[i] = oset.getFname();
            specNamesOut[i] = oset.getNamesOfOutputSpecies();

            if (oset.hasRegion()) {
                regionsOut[i] = oset.getRegion();
            } else {
                regionsOut[i] = "default"; // RC uses "default" as default value
            }
            specIndexesOut[i] = new int[specNamesOut[i].length];

            for (int k = 0; k < specNamesOut[i].length; k++) {
                for (int kq = 0; kq < nspec; kq++) {

                    if (specNamesOut[i][k].equalsIgnoreCase(specieIDs[kq])) {
                        specIndexesOut[i][k] = kq;
                    }
                }
            }
        }
    }






    public void extractGrid() {
        Morphology morph = sdRun.getMorphology();
        TreePoint[] tpa = morph.getTreePoints();
        Discretization disc = sdRun.getDiscretization();

        double d = disc.defaultMaxElementSide;
        if (d <= 0) {
            d = 1.;
        }

        // <--WK 6 22 2007
        // (1) iterate through all endpoints and their associated radii.
        // (2) divide each radius by successively increasing odd numbers until
        // the divided value becomes less than the defaultMaxElementSide.
        // (3) select the smallest among the divided radii values as d.
        double[] candidate_grid_sizes = new double[tpa.length];
        for (int i = 0; i < tpa.length; i++) {
            double diameter = tpa[i].r * 2.0;
            double denominator = 1.0;
            while ((diameter / denominator) > d) {
                denominator += 2.0; // divide by successive odd numbers
            }
            candidate_grid_sizes[i] = diameter / denominator;
        }

        double min_grid_size = d;
        for (int i = 0; i < tpa.length; i++) {
            if (candidate_grid_sizes[i] < min_grid_size)
                min_grid_size = candidate_grid_sizes[i];
        }

        d = min_grid_size;

        // E.info("subvolume grid size is: " + min_grid_size);


        int vgg = VolumeGrid.GEOM_2D;

        // REFAC - elsewhere;
        String sg = sdRun.geometry;
        if (sg != null) {
            if (sg.toLowerCase().equals("2d")) {
                vgg = VolumeGrid.GEOM_2D;

            } else if (sg.toLowerCase().equals("3d")) {
                vgg = VolumeGrid.GEOM_3D;
            } else {
                E.warning("unrecognized geometry " + sg + " should be 2D or 3D");
            }
        }

        double d2d = sdRun.depth2D;
        if (d2d <= 0.) {
            d2d = 0.5;
        }


        if (disc.curvedElements()) {
            TreeCurvedElementDiscretizer tced = new TreeCurvedElementDiscretizer(tpa);
            volumeGrid = tced.buildGrid(d, disc.getResolutionHM(), disc.getSurfaceLayers(), disc.getMaxAspectRatio());

        } else {
            TreeBoxDiscretizer tbd = new TreeBoxDiscretizer(tpa);
            volumeGrid = tbd.buildGrid(d, disc.getResolutionHM(), disc.getSurfaceLayers(),  vgg, d2d);
        }

        SpineLocator spineloc = new SpineLocator(sdRun.spineSeed, morph.getSpineDistribution(), disc.spineDeltaX);

        spineloc.addSpinesTo(volumeGrid);

        volumeGrid.fix();

        makeRegionConcentrations(volumeGrid.getRegionLabels());
        makeRegionSurfaceDensities(volumeGrid.getRegionLabels());

    }

    public final boolean useBinomial() {
        return (distID == distribution_t.BINOMIAL);
    }

    public final boolean usePoisson() {
        return (distID == distribution_t.POISSON);
    }

    public final boolean doIndependent() {
        return (algoID == INDEPENDENT);
    }

    public final boolean doShared() {
        return (algoID == SHARED);
    }

    public final boolean doParticle() {
        return (algoID == PARTICLE);
    }

    public double[] getNanoMolarConcentrations() {
        return baseConcentrations;
    }

    public double[][] getRegionConcentrations() {
        if (regionConcentrations == null) {
            extractGrid();
        }
        return regionConcentrations;
    }

    public double[][] getRegionSurfaceDensities() {
        return regionSurfaceDensities;
    }


    public double[][] getRevisedRegionConcentrations() {
        if (regionConcentrations == null) {
            extractGrid();
        }
        makeRegionConcentrations(volumeGrid.getRegionLabels());
        return regionConcentrations;
    }


    public double[][] getRevisedRegionSurfaceDensities() {
        makeRegionSurfaceDensities(volumeGrid.getRegionLabels());
        return regionSurfaceDensities;
    }




    private void makeRegionConcentrations(String[] sra) {
        InitialConditions icons = sdRun.getInitialConditions();
        int nc = baseConcentrations.length;
        double[][] ret = new double[sra.length][];
        for (int i = 0; i < sra.length; i++) {
            // RCC now we get the base concentrations everywhere, and just
            // override
            // those values that are explicitly set elsewhere
            ret[i] = new double[baseConcentrations.length];
            for (int j = 0; j < nc; j++) {
                ret[i][j] = baseConcentrations[j];
            }

            if (icons.hasConcentrationsFor(sra[i])) {
                double[] wk = icons.getRegionConcentrations(sra[i], speciesList);
                for (int j = 0; j < nc; j++) {
                    if (wk[j] >= 0.) {
                        ret[i][j] = wk[j];
                    }
                }
            }
        }
        regionConcentrations = ret;
    }

    private void makeRegionSurfaceDensities(String[] sra) {
        InitialConditions icons = sdRun.getInitialConditions();
        double[][] ret = new double[sra.length][];
        for (int i = 0; i < sra.length; i++) {
            if (icons.hasSurfaceDensitiesFor(sra[i])) {
                ret[i] = icons.getRegionSurfaceDensities(sra[i], speciesList);
            } else {
                ret[i] = null;
            }
        }
        regionSurfaceDensities = ret;
    }

    public long getCalculationSeed() {
        long ret = sdRun.simulationSeed;
        if (ret <= 0) {
            ret = (long)(1.e5 * Math.random());
        }
        return ret;
    }

    public ReactionTable getReactionTable() {
        if (reactionTable == null) {
            extractTables();
        }
        return reactionTable;
    }

    public StimulationTable getStimulationTable() {
        if (stimulationTable == null) {
            extractTables();
        }
        return stimulationTable;
    }

    public VolumeGrid getVolumeGrid() {
        if (volumeGrid == null) {
            extractGrid();
        }
        return volumeGrid;
    }

    public void setResultWriter(ResultWriter rw) {
        resultWriter = rw;
        resultWriter.init("cctdif2d", ResultWriter.TEXT); // others....
    }

    public abstract int run();

    public abstract long getParticleCount();


    public double[][] readInitialState(String fnm, int nel, int nspec, String[] specids) {
        String sdata = resultWriter.readSibling(fnm);
        SDState sds = StateReader.readStateString(sdata);

        double[][] ret = null;
        if (sds.nel == nel && sds.nspec == nspec) {
            if (ArrayUtil.arraysMatch(sds.specids, specids)) {
                ret = sds.getData();
            } else {
                E.error("initial conditions species mismatch ");
                for (int i = 0; i < specids.length; i++) {
                    E.info("species " + i + " " + specids[i] + " " + sds.specids[i]);
                }
            }
        } else {
            E.error("initial conditions file does not match model: elements " + nel + ", " + sds.nel +
                    "  species: " + nspec + ", " + sds.nspec);
        }


        return ret;

    }


}
