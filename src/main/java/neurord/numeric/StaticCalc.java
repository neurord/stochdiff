package neurord.numeric;

import java.util.Arrays;

import neurord.model.SDRun;
import static neurord.model.OutputSet.outputSpecieIndices;
import neurord.numeric.chem.ReactionTable;
import neurord.numeric.morph.VolumeGrid;

public class StaticCalc extends BaseCalc {
    ReactionTable rtab;
    VolumeGrid vgrid;
    int nel;
    int nspec;
    String[] species;
    double[] volumes;
    boolean[] submembranes;
    String[] regionLabels;
    int[] eltregions;
    double[] surfaceAreas;

    double[][] wkA;


    public StaticCalc(int trial, SDRun sdr) {
        super(trial, sdr);
    }

    @Override
    public long getParticleCount() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    protected void _run() {
        // TODO Auto-generated method stub
    }


    public void init() {

        rtab = this.sdRun.getReactionTable();
        vgrid = this.sdRun.getVolumeGrid();

        nel = vgrid.size();

        nspec = rtab.getNSpecies();
        species = rtab.getSpecies();



        volumes = vgrid.getElementVolumes();

        /*
        fdiff = rtab.getDiffusionConstants();
        neighbors = vgrid.getPerElementNeighbors();
        couplingConstants = vgrid.getPerElementCouplingConstants();
        */

        submembranes = vgrid.getSubmembranes();
        regionLabels = vgrid.getRegionLabels();

        surfaceAreas = vgrid.getExposedAreas();

        wkA = new double[nel][nspec];

    }



    public void reinit() {
        VolumeGrid grid = this.sdRun.getVolumeGrid();

        for (int i = 0; i < nel; i++) {
            double[] rcs = this.sdRun.getRegionConcentration(grid.getElementRegion(i));
            for (int j = 0; j < nspec; j++) {
                wkA[i][j] = rcs[j];
            }

            double a = surfaceAreas[i];
            if (a > 0) {
                double[] scs = this.sdRun.getRegionSurfaceDensity(grid.getElementRegion(i));
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
        return outputSpecieIndices(this.getClass().getName(),
                                   Arrays.asList(sid),
                                   this.sdRun.getSpecies());
    }
}
