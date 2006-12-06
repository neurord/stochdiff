package org.textensor.stochdiff.numeric;

import org.textensor.report.E;
import org.textensor.stochdiff.ResultWriter;
import org.textensor.stochdiff.disc.SpineLocator;
import org.textensor.stochdiff.disc.TreeBoxDiscretizer;
import org.textensor.stochdiff.model.*;
import org.textensor.stochdiff.numeric.chem.ReactionTable;
import org.textensor.stochdiff.numeric.chem.StimulationTable;
import org.textensor.stochdiff.numeric.morph.TreePoint;
import org.textensor.stochdiff.numeric.morph.VolumeGrid;


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



    public BaseCalc(SDRun sdr) {
        sdRun = sdr;
    }


    public void extractTables() {
        ReactionScheme rsch = sdRun.getReactionScheme();

        reactionTable= rsch.makeReactionTable();

        StimulationSet stim = sdRun.getStimulationSet();
        stimulationTable = stim.makeStimulationTable(reactionTable);

        InitialConditions icons = sdRun.getInitialConditions();
        speciesList = reactionTable.getSpeciesIDs();
        // double vol = sdRun.poolVolume;
        baseConcentrations = icons.getDefaultNanoMolarConcentrations(speciesList);
    }



    public void extractGrid() {
        Morphology morph = sdRun.getMorphology();
        TreePoint[] tpa = morph.getTreePoints();
        Discretization disc = sdRun.getDiscretization();


        double d = disc.defaultMaxElementSide;
        if (d <= 0) {
            d = 1.;

        }
        TreeBoxDiscretizer tbd = new TreeBoxDiscretizer(tpa);

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
        volumeGrid = tbd.buildGrid(d, disc.getResolutionHM(), vgg, d2d);



        SpineLocator spineloc = new SpineLocator(sdRun.spineSeed,
                morph.getSpineDistribution(), disc.spineDeltaX);

        spineloc.addSpinesTo(volumeGrid);

        volumeGrid.fix();

        makeRegionConcentrations(volumeGrid.getRegionLabels());
        makeRegionSurfaceDensities(volumeGrid.getRegionLabels());

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



    private void makeRegionConcentrations(String[] sra) {
        InitialConditions icons = sdRun.getInitialConditions();
        int nc = baseConcentrations.length;
        double[][] ret = new double[sra.length][];
        for (int i = 0; i < sra.length; i++) {
            if (icons.hasConcentrationsFor(sra[i])) {
                ret[i] = icons.getRegionConcentrations(sra[i], speciesList);
            } else {
                // could also leave at zero?
                ret[i] = new double[baseConcentrations.length];
                for (int j = 0; j < nc; j++) {
                    ret[i][j] = baseConcentrations[j];
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


    public abstract void run();

}
