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

        double d = sdRun.maxElementSide;
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

        volumeGrid = tbd.buildGrid(d, vgg);

        SpineLocator spineloc = new SpineLocator(sdRun.spineSeed,
                morph.getSpineDistribution(), sdRun.spineDeltaX);

        spineloc.addSpinesTo(volumeGrid);

        volumeGrid.fix();

        makeRegionConcentrations(volumeGrid.getRegionLabels());

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
