//5 13 2010 RO Methods used to save alternative files "*conc.txt" included for deterministic
//             copied from SteppedStochasticGridCalc.java, getGridConcsPlainText, getGridConcsPlainText_dumb,
//             getGridConcsHeadings, getGridConcsHeadings_dumb, stringi and stringd
//             Other variables have been included in class DeterministicGridCalc following
//             the template from SteppedStochasticGridCalc in order to save files "*conc.txt"
package org.textensor.stochdiff.numeric.grid;

import java.util.Arrays;

import org.textensor.report.E;
import org.textensor.stochdiff.model.SDRun;
import org.textensor.stochdiff.numeric.BaseCalc;
import org.textensor.stochdiff.numeric.math.Column;
import org.textensor.stochdiff.numeric.math.Matrix;
import org.textensor.stochdiff.numeric.morph.VolumeGrid;

import org.textensor.util.ArrayUtil;

/*
 *
 * Dufort-Frankel reaction diffusion implementation.
 * This is a continuous (deterministic) calculation to generate reference data.
 */

public class DeterministicGridCalc extends GridCalc {

    @Override
    public boolean preferConcs(){ return true; }

//	int[] eltstims;
//	double[] eltstimshare;
    //AB Dec 16 2012
    //make these two arrays 2D to hold info on each stimulation.  Was being overwritten by last stim
    int[][] eltstims;
    double[][] eltstimshare;

//AB 2012 Apr 3: change wkB to wktm1 which stands for wk(time-1)
    //purpose is to write out the present time array: wkA
    //this completely fixed the discrepancy between stoch and determ!!!
    double[][] wkA;
    double[][] wktm1;
    double[][] wkC;

    int nlog;

    public DeterministicGridCalc(SDRun sdm) {
        super(sdm);
    }

    // the only task here is to allocate and initialize the workspace for
    // the calculation: three arrays for each species at each element;
    public final void init() {
        super.init();

        nspec = rtab.getNSpecies();

        volumes = vgrid.getElementVolumes();

        fdiff = rtab.getDiffusionConstants();
        neighbors = vgrid.getPerElementNeighbors();
        couplingConstants = vgrid.getPerElementCouplingConstants();

        extractOutputScheme(rtab); // see BaseCalc.java

        stimTab = getStimulationTable();
        stimtargets = vgrid.getAreaIndexes(stimTab.getTargetIDs());
        // eltstims gives the index in the stim array for
        // the stim to element i, if any. -1 otherwise
        /* AB Dec 16 2011 - eltstims needs to be 2D, determine target element and share for each */
        int numStim=stimTab.getNStim();
        eltstims = new int[numStim][nel];
        eltstimshare = new double[numStim][nel];
        for (int stimnum = 0; stimnum < numStim; stimnum++) {
            for (int i = 0; i < eltstims[stimnum].length; i++) {
                eltstims[stimnum][i] = -1;
            }
        }
        for (int i = 0; i < stimtargets.length; i++) {
            //asti are the list of voxels to receive particles
            int[] asti = stimtargets[i];
            double vtot = 0.;
            for (int k = 0; k < asti.length; k++) {
                vtot += volumes[asti[k]];
            }
//AB 12-19-11: changed volumes[i] to volumes[asti[k]]
            for (int k = 0; k < asti.length; k++) {
                eltstims[i][asti[k]] = i;
                eltstimshare[i][asti[k]] = volumes[asti[k]] / vtot;
            }
        }

        surfaceAreas = vgrid.getExposedAreas();
//AB 2012-apr 3 change wkB to wk[time-1]
        wkA = new double[nel][nspec];
        wktm1 = new double[nel][nspec];
        wkC = new double[nel][nspec];

        dt = sdRun.fixedStepDt;

        double[][] regcon = getRegionConcentrations();
        double[][] regsd = getRegionSurfaceDensities();

        for (int i = 0; i < nel; i++) {
            double[] rcs = regcon[eltregions[i]];
            for (int j = 0; j < nspec; j++) {
                wkA[i][j] = rcs[j];
                //AB 2012-apr 3 change wkB to wk[time-1]
                wktm1[i][j] = rcs[j];
                wkC[i][j] = rcs[j];
            }

            double a = surfaceAreas[i];
            double[] scs = regsd[eltregions[i]];
            if (a > 0 && scs != null) {

                // the actual concentration in the surface elements depends how
                // deep they are
                // scs in in nanomoles per square micron (check) surfaceArea in
                // square micron, so need to multiply
                // by surfaceArea to get nanomoles and divide by volume for
                // actual concentration
                double concfac = a / volumes[i];

                for (int j = 0; j < nspec; j++) {
                    if (Double.isNaN(scs[j])) {
                        // means not specified by the user;

                    } else {
                        wkA[i][j] = concfac * scs[j];
                        //AB 2012-apr 3 change wkB to wk[time-1]
                        wktm1[i][j] = concfac * scs[j];
                        wkC[i][j] = concfac * scs[j];
                    }
                }
            }
        }

        if (sdRun.initialStateFile != null) {
            double[][] cc = (double[][])
                resultWriters.get(0).loadState(sdRun.initialStateFile, this);
            ArrayUtil.copy(cc, this.wkA);
            ArrayUtil.copy(cc, this.wktm1);
        }
    }

    public final int run() {
        init();

        assert this.resultWriters.size() > 0;

        double time = sdRun.getStartTime();
        double endtime = sdRun.getEndTime();

        for(ResultWriter resultWriter: this.resultWriters)
            resultWriter.writeGrid(vgrid, sdRun.getStartTime(), fnmsOut, this);

        for (int i = 0; i < fnmsOut.length; i++)
            for(ResultWriter resultWriter: this.resultWriters)
                resultWriter.writeGridConcsDumb(i, time, nel, fnmsOut[i], this);

        E.info("Running from time=" + time + "ms to time=" + endtime + "ms");

        double tlog = 5.;

        long startTime = System.currentTimeMillis();

        // int iwr = 0;
        double writeTime = -1.e-9;

        // RCC commenting this out to use desired output interval from model
        // spec.
        // sdRun.outputInterval = 100.0;
        double[] writeTimeArray = new double[fnmsOut.length];
        Arrays.fill(writeTimeArray, -1.e-9);

        int iwr = 0;
        // RO
        while (time < endtime) {

            // RO 5 13 2010: follows template in SteppedStochasticGridCalc
            if (time >= writeTime) {
                for(ResultWriter resultWriter: this.resultWriters)
                    resultWriter.writeGridConcs(time, nel, ispecout, this);

                writeTime += sdRun.outputInterval;
            }
            for (int i = 0; i < fnmsOut.length; i++)
                if (time >= writeTimeArray[i]) {
                    for(ResultWriter resultWriter: this.resultWriters)
                        resultWriter.writeGridConcsDumb(i, time, nel, fnmsOut[i], this);
                    writeTimeArray[i] += Double.valueOf(dtsOut[i]);
                }

            time += advance(time);

            if (time > tlog) {
                E.info("time " + time + " dt=" + dt);
                tlog += Math.max(50 * sdRun.outputInterval, 5);
            }


            if (time >= stateSaveTime) {
                for(ResultWriter resultWriter: this.resultWriters)
                    resultWriter.saveState(time, sdRun.stateSavePrefix, this);
                stateSaveTime += sdRun.getStateSaveInterval();
            }
        }

        long endTime = System.currentTimeMillis();
        E.info("total time " + (endTime - startTime) + "ms");

        return 0;
    }

    public double advance(double time) {
        // diffusion terms;
        // wkA is time t, wktm1 time t-1, wkC the next step, t+1

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

            // zr part not needed unless we put the source terms in wkC first
            for (int k = 0; k < nspec; k++) {
                zr[k] = wkC[iel][k] / vol;
                zl[k] = 1.;
            }

            int inbr[] = neighbors[iel];
            double gnbr[] = couplingConstants[iel];
            int nnbr = inbr.length;
            /*
            			for (int j = 0; j < nnbr; j++) {
            				for (int k = 0; k < nspec; k++) {
            					double ff = fvol * fdiff[k] * gnbr[j];
            					//AB 2012-apr 3 change wkB to wk[time-1], and wkA to wk[time]
            					zr[k] += ff * (wkA[inbr[j]][k] - 0.5 * wktm1[iel][k]);
            					zl[k] += 0.5 * ff;
            				}
            			}
            			for (int k = 0; k < nspec; k++) {
            				wkC[iel][k] = (wkA[iel][k] + zr[k]) / zl[k];
            			}
            */
            //AB 2012 Apr 4 - skip the diffusion if fdiff=0 to make this part faster
            for (int k = 0; k < nspec; k++) {
                if (fdiff[k]>0) {
                    for (int j = 0; j < nnbr; j++) {
                        double ff = fvol * fdiff[k] * gnbr[j];
                        zr[k] += ff * (wkA[inbr[j]][k] - 0.5 * wktm1[iel][k]);
                        zl[k] += 0.5 * ff;
                    }
                }
                wkC[iel][k] = (wkA[iel][k] + zr[k]) / zl[k];
            }
        }

        double[][] stims = stimTab.getStimsForInterval(time, dt);

        // reaction step;
        for (int iel = 0; iel < nel; iel++) {
//AB 12-19-11	create the coninc outside of the pinj loop, to accumulate injections from multiple stimuli
            double[] concinc=new double[nspec];
            int concnull=1;
            double fconc = NM_PER_PARTICLE_PUV / volumes[iel];
//AB 12-19-11 need to create loop over all the stimulations, and
//			create pinj for each stimulation (not just the first one)
            for (int stimnum=0; stimnum<stims.length; stimnum++) {
                if (eltstims[stimnum][iel] >= 0) {
                    concnull=0;
                    double[] pinj=stims[stimnum];

                    for (int i = 0; i < pinj.length; i++) {
                        concinc[i] += pinj[i] * fconc * eltstimshare[stimnum][iel];
                        if (concinc[i] < 0) {
                            E.error("negative concentration? " + concinc[i]);
                        }
                    }
                }
            }
            if (concnull==0) {
                reacStep(wkC[iel], dt, concinc);
            } else {
                reacStep(wkC[iel], dt, null);
            }
        }

        // cycle the solution arrays
        //AB 2012-apr 3 change wkB to wk[time-1]
        double[][] wkT = wktm1;
        wktm1 = wkA;
        wkA = wkC;
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
         * if (nlog < 5) { E.info("concs " + concs[0] + " " + concs[1] + " " +
         * concs[2]); nlog++; m.dump(); }
         */

        m.multiplyBy(dt);
        m.subtractIdentity();
        m.negate();

        /*
         * if (nlog < 5) { m.dump(); }
         */

        if (concinc != null) {
            cpdt.incrementBy(concinc);
        }

        Column dc = m.LUSolve(cpdt);
        col.incrementBy(dc);
        col.writeTo(concs); // TODO noop?
    }

    @Override
    public double getGridPartConc(int i, int j) {
        return wkA[i][j];
    }

    @Override
    public int getGridPartNumb(int i, int j) {
        double val = getGridPartConc(i, j);
        return (int) Math.round(val * volumes[i] * PARTICLES_PUVC);
    }

    @Override
    public int[][] getReactionEvents() {
        /* not implemented yet */
        return null;
    }

    @Override
    public int[][][] getDiffusionEvents() {
        /* not implemented yet */
        return null;
    }

    @Override
    public int[][] getStimulationTargets() {
        return this.stimtargets;
    }

    @Override
    public int[][] getStimulationEvents() {
        /* not implemented yet */
        return null;
    }
}
