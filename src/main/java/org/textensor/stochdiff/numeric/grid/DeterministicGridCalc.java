package org.textensor.stochdiff.numeric.grid;

import org.textensor.report.E;
import org.textensor.stochdiff.model.SDRun;
import org.textensor.stochdiff.numeric.BaseCalc;
import org.textensor.stochdiff.numeric.chem.ReactionTable;
import org.textensor.stochdiff.numeric.chem.StimulationTable;
import org.textensor.stochdiff.numeric.math.Column;
import org.textensor.stochdiff.numeric.math.Matrix;
import org.textensor.stochdiff.numeric.morph.VolumeGrid;


/*
 *
 * Dufort-Frankel reaction diffusion implementation.
 * This is a continuous (deterministic) calculation to generate reference data.
 */

public class DeterministicGridCalc extends BaseCalc {

    // converting particle numbers to concentrations
    // nanomoles per particle per unit volume
    // ie, each particle added to a cubic micron increases
    // the nanoMolar concentration this much
    final static double NM_PER_PARTICLE_PUV = 1. / 0.6022;


    Column mconc;

    ReactionTable rtab;
    VolumeGrid vgrid;
    StimulationTable stimTab;


    double dt;

    int nel;
    int nspec;
    String[] specieIDs;
    double[] volumes;
    double[] fdiff;

    int[][] neighbors;
    double[][] couplingConstants;

    int[][] stimtargets;
    int[] eltstims;
    double[] eltstimshare;

    int[] eltregions;

    double[][] wkA;
    double[][] wkB;
    double[][] wkC;



    int nlog;

    public DeterministicGridCalc(SDRun sdm) {
        super(sdm);
    }



    // the only task here is to allocate and initialize the workspace for
    // the calculation: three arrays for each species at each element;
    public final void init() {
        rtab = getReactionTable();
        vgrid = getVolumeGrid();


        nel = vgrid.getNElements();
        nspec = rtab.getNSpecies();
        specieIDs = rtab.getSpecieIDs();

        volumes = vgrid.getElementVolumes();

        fdiff = rtab.getDiffusionConstants();
        neighbors = vgrid.getPerElementNeighbors();
        couplingConstants = vgrid.getPerElementCouplingConstants();



        stimTab = getStimulationTable();
        stimtargets = vgrid.getAreaIndexes(stimTab.getTargetIDs());
        // eltstims gives the index in the stim array for
        // the stim to element i, if any. -1 otherwise
        eltstims = new int[nel];
        eltstimshare = new double[nel];
        for (int i = 0; i < eltstims.length; i++) {
            eltstims[i] = -1;
        }
        for (int i = 0; i < stimtargets.length; i++) {
            int[] asti = stimtargets[i];
            double vtot = 0.;
            for (int k = 0; k < asti.length; k++) {
                vtot += volumes[asti[k]];
            }

            for (int k = 0; k < asti.length; k++) {
                eltstims[asti[k]] = i;
                eltstimshare[asti[k]] = volumes[i] / vtot;
            }
        }

        eltregions = vgrid.getRegionIndexes();

        wkA = new double[nel][nspec];
        wkB = new double[nel][nspec];
        wkC = new double[nel][nspec];

        dt = sdRun.fixedStepDt;

        double[][] regcon = getRegionConcentrations();


        for (int i = 0; i < nel; i++) {
            double[] rcs = regcon[eltregions[i]];
            for (int j = 0; j < nspec; j++) {
                wkA[i][j] = rcs[j];
                wkB[i][j] = rcs[j];
                wkC[i][j] = rcs[j];
            }
        }
    }


    public final void run() {
        init();
        double time = 0.;
        double runtime = sdRun.runtime;

        if (resultWriter != null) {
            resultWriter.writeString(vgrid.getAsText());
        }

        double tlog = 5.;


        int iwr = 0;
        while (time < runtime) {
            time += advance(time);

            iwr += 1;
            if (iwr % 5 == 0) {
                if (resultWriter != null) {
                    resultWriter.writeString(getGridConcsText(time));
                }
            }

            if (time > tlog) {
                E.info("time " + time + " dt=" + dt);
                tlog += 5;
            }
        }
    }


    public double advance(double time) {
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


            //zr part not needed unless we put the source terms in wkC first
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




        double[][] stims = stimTab.getStimsForInterval(time, dt);

        // reaction step;
        for (int iel = 0; iel < nel; iel++) {
            if (eltstims[iel] >= 0) {
                // for the stimulus we need delta Concentration
                // corresponding to the injection of particles;
                double[] pinj = stims[eltstims[iel]];
                double[] concinc = new double[pinj.length];
                double fconc = NM_PER_PARTICLE_PUV / volumes[iel];

                for (int i = 0; i < pinj.length; i++) {
                    concinc[i] = pinj[i] * fconc * eltstimshare[iel];
                    if (concinc[i] < 0) {
                        E.error("negative concentration? " + concinc[i]);
                    }
                }

                reacStep(wkC[iel], dt, concinc);
            } else {
                reacStep(wkC[iel], dt, null);
            }
        }




        // cycle the solution arrays
        double[][] wkT = wkA;
        wkA = wkB;
        wkB = wkC;
        wkC = wkT;

        return dt;
    }



    private void reacStep(double[] concs, double deltat, double[] concinc) {
        // Semi-implicit euler - see pool/SemiImplicitEulerPoolCalc
        Column col = new Column(concs);
        Matrix m = rtab.getIncrementRateMatrix(col);
        Column cp = rtab.getProductionColumn(col);
        Column cpdt = cp.times(deltat);

        /*
        if (nlog < 5) {
           E.info("concs " + concs[0] + " " + concs[1] + " " + concs[2]);
           nlog++;
           m.dump();
        }
        */

        m.multiplyBy(dt);
        m.subtractIdentity();
        m.negate();

        /*
        if (nlog < 5) {
           m.dump();
        }
        */

        if (concinc != null) {
            cpdt.incrementBy(concinc);
        }

        Column dc = m.LUSolve(cpdt);
        col.incrementBy(dc);
        col.writeTo(concs);  // TODO noop?
    }



    @SuppressWarnings("boxing")
    private String getGridConcsText(double time) {
        StringBuffer sb = new StringBuffer();
        sb.append("gridConcentrations " + nel + " " + nspec + " " + time + " ");
        for (int i = 0; i < nspec; i++) {
            sb.append(specieIDs[i] + " ");
        }
        sb.append("\n");

        for (int i = 0; i < nel; i++) {
            // sb.append("");
            for (int j = 0; j < nspec; j++) {
                sb.append(String.format(" %g5 ", wkB[i][j]));
            }
            sb.append("\n");
        }
        return sb.toString();
    }


}
