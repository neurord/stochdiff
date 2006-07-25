package org.textensor.stochdiff.numeric.grid;

import org.textensor.stochdiff.model.SDRun;
import org.textensor.stochdiff.numeric.BaseCalc;
import org.textensor.stochdiff.numeric.chem.ReactionTable;
import org.textensor.stochdiff.numeric.math.Column;
import org.textensor.stochdiff.numeric.morph.VolumeGrid;


// deterministic single mixed pool;

/*
 * Units: concentrations are expressed in nM and volumes in cubic microns
 * So, in these units , one Litre is 10^15 and a 1M solution is 10^9
 * The conversion factor between concentrations and particle number is
 * therefore
 * nparticles = 6.022^23 * vol/10^15 * conc/10^9
 * ie, nparticles = 0.6022 * conc
 *
 * Method -
 *
 * Switches between continuous and stochastic according to particle numbers
 * The continuous calculation uses the Dufort-Frankel scheme
 *
 * Stochastic calculations:
 *
 *
 *
 */



public abstract class MixedGridCalc extends BaseCalc {

    public static final double PARTICLES_PUVC = 0.6022;
    // particles Per Unit Volume, Concentration


    Column mconc;

    ReactionTable rtab;
    VolumeGrid vgrid;


    double dt;

    int nel;
    int nspec;
    double[] volumes;
    double[] fdiff;

    int[][] neighbors;
    double[][] couplingConstants;

    double[][] wkA;
    double[][] wkB;
    double[][] wkC;

    int[][] nparticle;



    public MixedGridCalc(SDRun sdm) {
        super(sdm);
    }



    // the only task here is to allocate and initialize the workspace for
    // the calculation: three arrays for each species at each element;
    public final void init() {
        rtab = getReactionTable();
        vgrid = getVolumeGrid();

        nel = vgrid.getNElements();
        nspec = rtab.getNSpecies();
        volumes = vgrid.getElementVolumes();

        fdiff = rtab.getDiffusionConstants();
        neighbors = vgrid.getPerElementNeighbors();
        couplingConstants = vgrid.getPerElementCouplingConstants();


        wkA = new double[nel][nspec];
        wkB = new double[nel][nspec];
        wkC = new double[nel][nspec];


        double[] c0 = getNanoMolarConcentrations();
        dt = sdRun.fixedStepDt;

        for (int i = 0; i < nel; i++) {
            for (int j = 0; j < nspec; j++) {
                wkA[i][j] = c0[j];
                wkB[i][j] = c0[j];
                wkC[i][j] = c0[j];
            }
        }


    }


    public final void run() {
        double time = 0.;
        double runtime = sdRun.runtime;


        while (time < runtime) {
            time += advance();
        }
    }



    public double advance() {

        // diffusion terms;
        // wkA is time t-1, wkB time t, wkC the next step, t+1

        // initialize next step values to zero;
        for (int i = 0; i < nel; i++) {
            for (int j = 0; j < nspec; j++) {
                wkC[i][j] = 0.;
            }
        }


        // source terms;
        // should increment injectiosn sites by 2 * the injected quantity;


        double[] zl = new double[nspec];
        double[] zr = new double[nspec];


        // diffusion step;
        for (int iel = 0; iel < nel; iel++) {
            double vol = volumes[iel];
            double fvol = 2. * dt / vol; // 2 for the two leapfrog steps;


            for (int k = 0; k < nspec; k++) {
                zr[k] = wkC[iel][k] / vol;
                zl[k] = 1.;
            }

            int inbr[] = neighbors[iel];
            double gnbr[] = couplingConstants[iel];
            int nnbr = inbr.length;

            for (int j = 0; j < nnbr; j++) {
                for (int k = 0; k < nspec; k++) {
                    double ff = fvol * fdiff[k] * gnbr[j];
                    zr[k] += ff * (wkB[inbr[j]][k] - 0.5 * wkA[iel][k]);
                    zl[k] += 0.5 * ff;
                }
            }


            for (int k = 0; k < nspec; k++) {
                wkC[iel][k] = (wkA[iel][k] + zr[k]) / zl[k];
            }
        }


        // reaction step;
        for (int iel = 0; iel < nel; iel++) {
            reacStep(wkC[iel], dt);
        }


        // rotate the solution arrays
        double[][] wkT = wkA;
        wkA = wkB;
        wkB = wkC;
        wkC = wkT;


        return dt;
    }



    private void reacStep(double[] concs, double deltat) {

    }

}
