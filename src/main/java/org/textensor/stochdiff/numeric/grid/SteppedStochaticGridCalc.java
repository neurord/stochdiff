package org.textensor.stochdiff.numeric.grid;

import org.textensor.report.E;
import org.textensor.stochdiff.model.SDRun;
import org.textensor.stochdiff.numeric.BaseCalc;
import org.textensor.stochdiff.numeric.chem.ReactionTable;
import org.textensor.stochdiff.numeric.chem.StimulationTable;
import org.textensor.stochdiff.numeric.math.Column;
import org.textensor.stochdiff.numeric.math.MersenneTwister;
import org.textensor.stochdiff.numeric.morph.VolumeGrid;
import org.textensor.stochdiff.numeric.stochastic.InterpolatingStepGenerator;
import org.textensor.stochdiff.numeric.stochastic.StepGenerator;
import org.textensor.util.ArrayUtil;



/*
 * Approximate stochastic calculation with a fixed timestep where
 * the number of reactions orruring in a volume and the number of particles
 * moving to neighbouring volumes are both generated from lookup tables
 * in p (probability of event for one particle)  and
 * n (number of particles of a given type)  for a uniform random number.
 *
 * This is approximate because the effect of the particles on thier new
 * location is note taken into account until the following step.
 *
 *
 * Units: concentrations are expressed in nM and volumes in cubic microns
 * So, in these units , one Litre is 10^15 and a 1M solution is 10^9
 * The conversion factor between concentrations and particle number is
 * therefore
 * nparticles = 6.022^23 * vol/10^15 * conc/10^9
 * ie, nparticles = 0.6022 * conc
 *
 *
 */



public class SteppedStochaticGridCalc extends BaseCalc {

    // particles Per Unit Volume and Concentration
    public static final double PARTICLES_PUVC = 0.6022;

    public final static double CONC_OF_N = 1. / 0.6022;


    // particles per unit area and surface density
    // (is the same as PUVC - sd unit is picomoles per square metre)
    public static final double PARTICLES_PUASD = 0.6022;

    Column mconc;

    ReactionTable rtab;
    VolumeGrid vgrid;

    StimulationTable stimTab;

    double dt;

    int nel;
    int nspec;
    String[] specieIDs;
    double[] volumes;
    double[] lnvolumes;
    double[] fdiff;
    double[] lnfdiff;

    double[] surfaceAreas;

    int[][] neighbors;
    double[][] couplingConstants;
    double[][] lnCC;

    int[][] wkA;
    int[][] wkB;
    int[] wkReac;

    int[][] nparticle;

    int nreaction;
    public int nspecie;
    String[] speciesIDs;
    double[] diffusionConstants;


    int[][] reactantIndices;
    int[][] productIndices;

    int[][] reactantStochiometry;
    int[][] productStochiometry;


    double[] rates;
    double[] lnrates;

    int[][] stimtargets;


    double[] intlogs;
    double lndt;

    int ninjected = 0;

    InterpolatingStepGenerator interpSG;
    MersenneTwister random;
    int nwarn;

    double[][] pSharedOut;
    double[][] lnpSharedOut;
    double[][][] fSharedExit;

    public SteppedStochaticGridCalc(SDRun sdm) {
        super(sdm);
    }



    public final void init() {

        // something to generate the random nunmbers
        random = new MersenneTwister(getCalculationSeed());

        rtab = getReactionTable();

        nreaction = rtab.getNReaction();
        rates = rtab.getRates();
        lnrates = ArrayUtil.log(rates, -999.);

        reactantIndices = rtab.getReactantIndices();
        productIndices = rtab.getProductIndices();

        reactantStochiometry = rtab.getReactantStochiometry();
        productStochiometry = rtab.getProductStochiometry();

        vgrid = getVolumeGrid();

        nel = vgrid.getNElements();
        nspec = rtab.getNSpecies();
        specieIDs = rtab.getSpecieIDs();
        volumes = vgrid.getElementVolumes();
        lnvolumes = ArrayUtil.log(volumes, -999.);

        surfaceAreas = vgrid.getExposedAreas();

        fdiff = rtab.getDiffusionConstants();
        lnfdiff = ArrayUtil.log(fdiff, -999.);

        neighbors = vgrid.getPerElementNeighbors();
        couplingConstants = vgrid.getPerElementCouplingConstants();
        lnCC = ArrayUtil.log(couplingConstants, -999.);


        stimTab = getStimulationTable();
        stimtargets = vgrid.getAreaIndexes(stimTab.getTargetIDs());


        // workspace for the calculation
        wkA = new int[nel][nspec];
        wkB = new int[nel][nspec];
        wkReac = new int[nreaction];


        int[] eltregions = vgrid.getRegionIndexes();
        double[][] regcon = getRegionConcentrations();
        double[][] regsd = getRegionSurfaceDensities();


        // apply initial conditions over the grid
        for (int i = 0; i < nel; i++) {
            double v = volumes[i];
            double[] rcs = regcon[eltregions[i]];

            for (int j = 0; j < nspec; j++) {
                double rnp = v * rcs[j] * PARTICLES_PUVC;
                int irnp = (int)rnp;
                double drnp = rnp - irnp;

                // random allocation to implement the remainder of the
                // density (some cells get an extra particle, some dont)
                if (random.random() < drnp) {
                    irnp += 1;
                }
                wkA[i][j] = irnp;
                wkB[i][j] = irnp;
            }


            double a = surfaceAreas[i];
            double[] scs = regsd[eltregions[i]];
            if (a > 0 && scs != null) {
                for (int j = 0; j < nspec; j++) {
                    double rnp = a * scs[j] * PARTICLES_PUASD;
                    int irnp = (int)rnp;
                    double drnp = rnp - irnp;

                    // random allocation to implement the remainder of the
                    // density (some cells get an extra particle, some dont)
                    if (random.random() < drnp) {
                        irnp += 1;
                    }
                    wkA[i][j] += irnp;
                    wkB[i][j] += irnp;
                }


            }


            /*
             * if (i % 20 == 0) { E.info("elt " + i + " region " + eltregions[i] + "
             * n0 " + wkA[i][0]); }
             */
        }
        dt = sdRun.fixedStepDt;
        lndt = Math.log(dt);


        // take logs of integers once only and store;
        intlogs = new double[10000];
        intlogs[0] = -99;
        for (int i = 1; i < intlogs.length; i++) {
            intlogs[i] = Math.log(i);
        }

        // final things we need is something to generate particle numbers
        // for steps of given n, p
        if (useBinomial())
            interpSG = InterpolatingStepGenerator.getBinomialGenerator();
        else if (usePoisson()) {
            interpSG = InterpolatingStepGenerator.getPoissonGenerator();
        } else {
            E.error("unknown probability distribution");
        }



        if (doShared() || doParticle()) {
            if (doShared()) {
                E.info("Using SHARED destination allocation");
            } else {
                E.info("Using PER PARTICLE destination allocation");
            }
            lnpSharedOut = new double[nel][nspec];
            pSharedOut = new double[nel][nspec];
            fSharedExit = new double[nel][nspec][8];
            for (int iel = 0; iel < nel; iel++) {
                for (int k = 0; k < nspec; k++) {
                    int inbr[] = neighbors[iel];
                    double lngnbr[] = lnCC[iel];
                    int nnbr = inbr.length;
                    int np0 = wkA[iel][k];

                    double ptot = 0.;
                    double[] pcnbr = new double[nnbr];

                    for (int j = 0; j < nnbr; j++) {
                        double lnpgo = lnfdiff[k] + lngnbr[j] + lndt - lnvolumes[iel];
                        // probability is dt * K_diff * contact_area /
                        // (center_to_center_distance * source_volume)
                        // gnbr contains the gometry: contact_area / distance

                        double p = Math.exp(lnpgo);
                        ptot += p;
                        pcnbr[j] = ptot;
                    }

                    double lnptot = Math.log(ptot);
                    if (lnptot > -1.) {
                        if (nwarn < 4) {
                            E.shortWarning("p too large at element " + iel + " species "  + k +
                                           " - capping from " + Math.exp(lnptot) + " to " + Math.exp(-1.));
                            nwarn++;
                        }
                        lnptot= -1.;
                    }

                    pSharedOut[iel][k] = ptot;
                    lnpSharedOut[iel][k] = lnptot;
                    for (int j = 0; j < nnbr; j++) {
                        fSharedExit[iel][k][j]  = pcnbr[j] / ptot;
                    }
                }
            }
        }
    }



    @SuppressWarnings("boxing")
    private String getGridConcsText(double time) {
        StringBuffer sb = new StringBuffer();
        // TODO tag specific to integer quantities;

        sb.append("gridConcentrations " + nel + " " + nspec + " " + time + " ");
        for (int i = 0; i < nspec; i++) {
            sb.append(specieIDs[i] + " ");
        }
        sb.append("\n");

        for (int i = 0; i < nel; i++) {
            for (int j = 0; j < nspec; j++) {
                sb.append(stringd((CONC_OF_N * wkA[i][j] / volumes[i])));
            }
            sb.append("\n");
        }
        return sb.toString();
    }


    @SuppressWarnings("boxing")
    private String getGridConcsPlainText(double time) {
        StringBuffer sb = new StringBuffer();
        // TODO tag specific to integer quantities;
        sb.append(stringd(time));
        for (int i = 0; i < nel; i++) {
            for (int j = 0; j < nspec; j++) {
                sb.append(stringd((CONC_OF_N * wkA[i][j] / volumes[i])));
            }
        }
        sb.append("\n");

        return sb.toString();
    }


    private String stringd(double d) {
        if (d == 0.0) {
            return "0.0 ";
        } else {
            return String.format("%.5g ", new Double(d));
        }
    }


    private String getGridConcsHeadings() {
        StringBuffer sb = new StringBuffer();
        sb.append("time ");
        for (int i = 0; i < nel; i++) {
            for (int j = 0; j < nspec; j++) {
                sb.append(" Vol_" + i + "_Spc_" + j + " ");
            }
        }
        sb.append("\n");
        return sb.toString();
    }


    public final void run() {
        init();

        if (resultWriter != null) {
            resultWriter.writeString(vgrid.getAsText());
            resultWriter.writeToSiblingFile(vgrid.getAsTableText(), "-mesh.txt");
            resultWriter.writeToSiblingFile(getGridConcsHeadings(), "-conc.txt");
        }

        double time = 0.;
        double runtime = sdRun.runtime;

        double tlog = 5.;


        long startTime = System.currentTimeMillis();

        // int iwr = 0;
        double writeTime = -1.e-9;
        while (time < runtime) {

            if (time > writeTime) {
                if (resultWriter != null) {
                    resultWriter.writeString(getGridConcsText(time));
                    resultWriter.writeToSiblingFile(getGridConcsPlainText(time), "-conc.txt");
                }
                writeTime += sdRun.outputInterval;
            }


            time += advance(time);

            if (time > tlog) {
                E.info("time " + time + " dt=" + dt);
                tlog += 5;
            }
        }

        long endTime = System.currentTimeMillis();
        E.info("total time " + (endTime - startTime) + "ms");

    }



    // NB the following method is one of the only two that need optimizing
    // (the other is nGo in the interpolating step generator)
    // things to do (in the c version)
    // - use BLAS calls for array operations,
    // - remove the two remaining exps
    // - unwrap inner conditionals for different reaction types
    // - make nGo inlinable


    public double advance(double tnow) {

        // add in any injections
        double[][] stims = stimTab.getStimsForInterval(tnow, dt);
        for (int i = 0; i < stims.length; i++) {
            double[] astim = stims[i];
            for (int j = 0; j < astim.length; j++) {
                if (astim[j] > 0.) {
                    // the stimulus could be spread over a number of elements
                    // as yet, assume equal probability of entering any of these
                    // elements (TODO)
                    // the random < asr ensures we get the right number of
                    // particles even the average entry per volume is less than one
                    // TODO - allow stim type (deterministic or poisson etc) in
                    // config;

                    int nk = stimtargets[i].length;
                    if (nk > 0) {
                        double as = astim[j] / nk;
                        int ias = (int)as;
                        double asr = as - ((int)as);

                        for (int k = 0; k < nk; k++) {
                            int nin = (ias + (random.random() < asr ? 1 : 0));
                            ninjected += nin;

                            wkA[stimtargets[i][k]][j] += nin;
                        }
                    }
                }
            }
        }

        // initialize wkB to the current values.
        // It will hold the midstep values for the leapfrog, after diffusion
        // but before reactions.
        for (int i = 0; i < nel; i++) {
            for (int j = 0; j < nspec; j++) {
                wkB[i][j] = wkA[i][j];
            }
        }


        // diffusion step;
        for (int iel = 0; iel < nel; iel++) {



            for (int k = 0; k < nspec; k++) {
                if (lnfdiff[k] > -90) {

                    int np0 = wkA[iel][k];

                    if (np0 > 0) {

                        if (algoID == INDEPENDENT) {
                            parallelDiffusionStep(iel, k);

                        } else if (algoID == SHARED) {
                            sharedDiffusionStep(iel, k);

                        } else if (algoID == PARTICLE) {
                            particleDiffusionStep(iel, k);
                        }
                    }
                }
            }
        }


        // for the reaction step, the source array is wkB and the
        // destination is wkA
        for (int i = 0; i < nel; i++) {
            for (int j = 0; j < nspec; j++) {
                wkA[i][j] = wkB[i][j];
            }
        }


        // reaction step;
        for (int iel = 0; iel < nel; iel++) {
            double lnvol = lnvolumes[iel];

            // start and end quantities for each species in a single
            // volume
            int[] nstart = wkB[iel];
            int[] nend = wkA[iel];
            for (int isp = 0; isp < nspecie; isp++) {
                nend[isp] = nstart[isp];
            }


            for (int ireac = 0; ireac < nreaction; ireac++) {
                // total number of possible reactions is the number of
                // particles in the smallest reactant population

                int[] ri = reactantIndices[ireac];
                int[] pi = productIndices[ireac];

                int[] rs = reactantStochiometry[ireac];
                int[] ps = productStochiometry[ireac];

                double lnp = lnrates[ireac] + lndt;


                int n = nstart[ri[0]];
                lnp += intlog(n);
                if (ri[1] >= 0) {
                    int nk = nstart[ri[1]];
                    lnp += intlog(nk);
                    lnp -= lnvol;
                    if (nk < n) {
                        n = nk;
                    }
                }


                if (lnp > -1.) {
                    if (nwarn < 5) {
                        E.shortWarning("p too large at element " + iel + " reaction " + ireac
                                       + " capping from " + Math.exp(lnp) + " to " + " exp(-1.)");
                        nwarn++;
                    }
                    lnp = -1.;
                }



                if (n <= 0) {

                } else {
                    int ngo = 0;
                    if (n == 1) {
                        // TODO use table to get rid of exp
                        ngo = (random.random() < Math.exp(lnp) ? 1 : 0);
                    } else if (n < interpSG.NMAX_STOCHASTIC) {
                        ngo = interpSG.nGo(n, lnp, random.random());

                    } else {
                        ngo = StepGenerator.gaussianStep(n, Math.exp(lnp), random.gaussian());
                    }


                    // update the new quantities in npn;
                    int ri0 = ri[0];
                    int ri1 = ri[1];
                    int rs0 = rs[0];
                    int rs1 = rs[1];

                    int navail = nend[ri0] / rs[0];
                    if (ri1 >= 0 && navail < nend[ri1] / rs1) {
                        navail = nend[ri1] / rs1;
                    }
                    if (ngo > navail) {
                        ngo = navail;
                        // TODO as for diffusion, we've got more particles going
                        // than there actually are. Should regenerate all
                        // reactions on theis element
                        // or use a binomial to share them out
                        // or use a smaller timestep;
                    }


                    nend[ri0] -= ngo * rs0;
                    if (ri1 >= 0) {
                        nend[ri1] -= ngo * rs1;
                    }

                    int pi0 = pi[0];
                    int pi1 = pi[1];

                    nend[pi0] += ngo * ps[0];
                    if (pi1 >= 0) {
                        nend[pi1] += ngo * ps[1];
                    }

                    // TODO this "if (ri[1] >= 0)" business is not great
                    // it applies for the A+B->C case, where there is a
                    // second reactant. We could probably do better by
                    // unrolling the four cases into separate blocks according
                    // to the reaction type
                    // - a good case for code generation.
                }
            }
        }

        // now wkA contains the actual numbers again;
        return dt;
    }




    private final void parallelDiffusionStep(int iel, int k) {
        int inbr[] = neighbors[iel];
        double lngnbr[] = lnCC[iel];
        int nnbr = inbr.length;
        int np0 = wkA[iel][k];

        for (int j = 0; j < nnbr; j++) {
            // use logs here so the operations are all additions
            // and the compiler should be able to be clever

            double lnpgo = lnfdiff[k] + lngnbr[j] + lndt - lnvolumes[iel];
            // probability is dt * K_diff * contact_area /
            // (center_to_center_distance * source_volume)
            // gnbr contains the gometry: contact_area / distance



            if (lnpgo > -1.) {
                if (nwarn < 4) {
                    E.shortWarning("p too large at element " + iel + " transition " + j + " to  "
                                   + inbr[j] + " - capping " + Math.exp(lnpgo) + " coupling is " + lngnbr[j]);
                    nwarn++;
                }
                lnpgo = -1.;
            }


            int ngo = 0;
            if (np0 == 1) {
                // TODO - use table anyway - avoid exp!
                ngo = (random.random() < Math.exp(lnpgo) ? 1 : 0);

            } else if (np0 < interpSG.NMAX_STOCHASTIC) {
                ngo = interpSG.nGo(np0, lnpgo, random.random());

            } else {
                ngo = StepGenerator.gaussianStep(np0, Math.exp(lnpgo), random.gaussian());
            }






            if (ngo > wkB[iel][k]) {
                if (nwarn < 10) {
                    E.shortWarning("ran out of particles - curtailing last transition from " + ngo + " to "
                                   + wkB[iel][k] + " leaving point " + iel + " species " + k);
                } else if (nwarn == 10) {
                    E.info("Suppressing future warnings");
                }
                nwarn++;

                ngo = wkB[iel][k];
                // TODO probably worth flagging if this ever happens
                // it means your steps could be too large
                // MATH if it does happen, there is a consistent
                // bias in that the last exit is the one that
                // is curtailed. We should actually restart
                // this set of jumps and get new fluxes to all
                // neighbours
            }

            wkB[iel][k] -= ngo;
            wkB[inbr[j]][k] += ngo;
        }
    }




    private final void sharedDiffusionStep(int iel, int k) {
        int np0 = wkA[iel][k];
        int inbr[] = neighbors[iel];
        int nnbr = inbr.length;
        double[] fshare = fSharedExit[iel][k];
        double lnptot = lnpSharedOut[iel][k];

        int ngo = 0;
        if (np0 == 1) {
            // TODO - use table anyway - avoid exp!
            ngo = (random.random() < Math.exp(lnptot) ? 1 : 0);


        } else if (np0 < interpSG.NMAX_STOCHASTIC) {
            ngo = interpSG.nGo(np0, lnptot, random.random());

        } else {
            ngo = StepGenerator.gaussianStep(np0, Math.exp(lnptot), random.gaussian());
        }





        wkB[iel][k] -= ngo;
        for (int i = 0; i < ngo; i++) {
            double r =  random.random();
            int io = 0;
            while (r > fshare[io]) {
                io++;
            }
            wkB[inbr[io]][k] += 1;
        }
    }

    private final void particleDiffusionStep(int iel, int k) {
        int np0 = wkA[iel][k];
        int inbr[] = neighbors[iel];
        int nnbr = inbr.length;
        double[] fshare = fSharedExit[iel][k];
        double ptot = pSharedOut[iel][k];

        for (int i = 0; i < np0; i++) {
            double r = random.random();

            if (r < ptot) {
                wkB[iel][k] -= 1;
                double fr = r / ptot;
                int io = 0;
                while (fr > fshare[io]) {
                    io++;
                }
                wkB[inbr[io]][k] += 1;
            }
        }
    }


    public final double intlog(int i) {
        double ret = 0.;
        if (i <= 0) {
            ret = -99.;
        } else {
            ret = (i < intlogs.length ? intlogs[i] : Math.log(i));
        }
        return ret;
    }


    public long getParticleCount() {
        long ret = 0;
        for (int i = 0; i < nel; i++) {
            for (int j = 0; j < nspec; j++) {
                ret += wkA[i][j];
            }
        }

        E.info("number injected = " + ninjected);
        return ret;
    }


}
