package org.textensor.stochdiff.numeric;

import org.textensor.report.E;
import org.textensor.stochdiff.model.SDRun;
import org.textensor.stochdiff.numeric.chem.ReactionTable;
import org.textensor.stochdiff.numeric.morph.VolumeGrid;

public class StaticCalc extends BaseCalc {
    public SDRun sdRun;


    ReactionTable rtab;
    VolumeGrid vgrid;
    int nel;
    int nspec;
    String[] specieIDs;
    double[] volumes;
    boolean[] submembranes;
    String[] regionLabels;
    int[] eltregions;
    double[] surfaceAreas;

    double[][] wkA;


    public StaticCalc(SDRun sdr) {
        super(sdr);
    }

    @Override
    public long getParticleCount() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int run() {
        // TODO Auto-generated method stub
        return 0;
    }


    public void init() {

        rtab = getReactionTable();
        vgrid = getVolumeGrid();

        nel = vgrid.getNElements();

        nspec = rtab.getNSpecies();
        specieIDs = rtab.getSpecieIDs();



        volumes = vgrid.getElementVolumes();

        /*
        fdiff = rtab.getDiffusionConstants();
        neighbors = vgrid.getPerElementNeighbors();
        couplingConstants = vgrid.getPerElementCouplingConstants();
        */

        submembranes = vgrid.getSubmembranes();
        regionLabels = vgrid.getRegionLabels();

        eltregions = vgrid.getRegionIndexes();
        surfaceAreas = vgrid.getExposedAreas();

        wkA = new double[nel][nspec];

    }



    public void reinit() {
        double[][] regcon = getRevisedRegionConcentrations();
        double[][] regsd = getRevisedRegionSurfaceDensities();

        for (int i = 0; i < nel; i++) {
            double[] rcs = regcon[eltregions[i]];
            for (int j = 0; j < nspec; j++) {
                wkA[i][j] = rcs[j];
            }

            double a = surfaceAreas[i];
            double[] scs = regsd[eltregions[i]];
            if (a > 0 && scs != null) {
                double concfac = a / volumes[i];

                for (int j = 0; j < nspec; j++) {
                    if (Double.isNaN(scs[j])) {
                        // means not specified by the user;

                    } else {
                        wkA[i][j] = concfac * scs[j];
                    }
                }
            }
        }

    }


    public int getNel() {
        return nel;
    }

    public int getNSpec() {
        return nspec;
    }

    public double[][] getElementConcentrations() {
        reinit();
        double[][] ret = new double[nel][nspec];
        for (int i = 0; i < nel; i++) {
            for (int j = 0; j < nspec; j++) {
                ret[i][j] = wkA[i][j];
            }
        }
        return ret;
    }


    public double[] getConcentrations() {
        reinit();
        double[] ret = new double[nel * nspec];
        for (int i = 0; i < nel; i++) {
            for (int j = 0; j < nspec; j++) {
                ret[nspec * i + j] = wkA[i][j];
            }
        }
        return ret;
    }

    public double[] getVolumes() {
        return volumes;
    }



    public int[] getSpecieIndexes(String[] sid) {
        int[] ret = new int[sid.length];
        for (int i = 0; i < sid.length; i++) {
            boolean got = false;
            for (int j = 0; j <specieIDs.length; j++) {
                if (sid[i].equals(specieIDs[j])) {
                    ret[i] = j;
                    got = true;
                }
            }
            if (!got) {
                E.error("Can't find species: " + sid[i]);
            }
        }
        return ret;
    }

}
