//7 11 2008 RO & WK: IF ( ( NumberofMolecules*(Probablity of diffusion or reaction) ) < NP && using binomial),
//          THEN calculate ngo from poisson distribution, ELSE calculate ngo from gaussian; NP=20.
//7 3 2008  RO: if observe negative ngo, ngo=0
//7 2 2008  WK: In parallelAndSharedDiffusionStep() function, when np0 > NMAX_STOCHASTIC and if n*p < 10,
//          then use poission to get ngo; otherwise, use gaussian
//9 25 2007 WK: In advance() function, we set the inc/decrements (i.e., ngo*xxx) to zero explicitly
//          to avoid floating point error
//9 11 2007 WK: In parallelAndSharedDiffusionStep(), for the independent diffusion,
//          (i) the probability to diffuse to a neighboring subvolume is the probability
//          to diffuse to that neighbor divided by the sum of all the probabilities
//          to diffuse to all neighbors; (ii) for calculating ngo, we use
//          binomial variance.
//8 28 2007 WK: In advance(), for the diffusion step, when algoID is INDEPENDENT,
//          we call parallelAndSharedDiffusionStep() instead of calling
//          parallelDiffusionStep().  WK added the parallelAndSharedDiffusionStep()
//          function and SHARED_DIFF_PARTICLES constant.
//6 18 2007 WK: The getGridConcsPlainText_dumb() function is modified to (i) flag
//           a volume element as either on submembrane or on cytosol, and
//           (ii) identify its region.
//5 16 2007: modified by RO & WK (modifications within initials ... initials)
//written by Robert Cannon
package org.textensor.stochdiff.numeric.grid;

import java.io.File;
import java.util.StringTokenizer;
import java.util.Arrays;

import org.textensor.report.Debug;
import org.textensor.report.E;
import org.textensor.stochdiff.model.SDRun;
import org.textensor.stochdiff.numeric.BaseCalc;
import org.textensor.stochdiff.numeric.math.RandomGenerator;
import org.textensor.stochdiff.numeric.math.MersenneTwister;
import org.textensor.stochdiff.numeric.morph.VolumeGrid;
import org.textensor.stochdiff.numeric.stochastic.InterpolatingStepGenerator;
import org.textensor.stochdiff.numeric.stochastic.StepGenerator;
import org.textensor.util.ArrayUtil;
import org.textensor.vis.CCViz3D;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


/*
 * Approximate stochastic calculation with a fixed timestep where
 * the number of reactions orruring in a volume and the number of particles
 * moving to neighbouring volumes are both generated from lookup tables
 * in p (probability of event for one particle)  and
 * n (number of particles of a given type)  for a uniform random number.
 *
 * This is approximate because the effect of the particles on their new
 * location is not taken into account until the following step.
 */

public class SteppedStochasticGridCalc extends GridCalc {
    static final Logger log = LogManager.getLogger(SteppedStochasticGridCalc.class);

    @Override
    public boolean preferConcs(){ return false; }

    // WK 8 28 2007
    // in parallelAndSharedDiffusionStep(),
    // (1) if the number of molecules to duffuse is less than
    // SHARED_DIFF_PARTICLES*(number of neighboring subvolume elements),
    // we do shared diffusion,
    // (2) otherwise, we do parallel diffusion.
    public static final int SHARED_DIFF_PARTICLES = 4;
    public static final int NP = 30;         // AB Changed from 20 to 30. 2011.09.23

    double[] lnfdiff;

    double[][] lnCC;

    int[][] wkA;
    int[][] wkB;

    double[] diffusionConstants;

    int[][] reactantIndices;
    int[][] productIndices;

    int[][] reactantStochiometry;
    int[][] productStochiometry;

    /**
     * Propensity powers for true second- and higher-order reactions.
     */
    int[][] reactantPowers;

    double[] lnvolumes;
    double[] lnrates;

    double lndt;

    InterpolatingStepGenerator interpSG;
    RandomGenerator random;
    int nngowarn = 0;         //added in v2.1.1 by BHK to keep track of a different type of warning

    double[][] pSharedOut;
    double[][][] fSharedExit;

    public SteppedStochasticGridCalc(SDRun sdm) {
        super(sdm);
    }

    protected int randomRound(double number) {
        int i = (int) number;
        double d = number - i;

        // random allocation to implement the remainder
        // (some cells get an extra particle, some don't)
        if (random.random() < d)
            i++;

        return i;
    }

    public final void init() {
        super.init();

        // something to generate the random nunmbers
        random = new MersenneTwister(getCalculationSeed());

        // Debug.dump("rates", rates);

        lnrates = ArrayUtil.log(rtab.getRates());
        log.debug("lnrates: {}", lnrates);

        reactantIndices = rtab.getReactantIndices();
        productIndices = rtab.getProductIndices();
        reactionEvents = new int[nel][rtab.getNReaction()];

        reactantStochiometry = rtab.getReactantStochiometry();
        productStochiometry = rtab.getProductStochiometry();
        reactantPowers = rtab.getReactantPowers();

        lnvolumes = ArrayUtil.log(volumes);
        log.debug("lnvolumes: {}", lnvolumes);

        lnfdiff = ArrayUtil.log(fdiff);

        lnCC = ArrayUtil.log(couplingConstants);
        diffusionEvents = new int[nel][nspec][];
        for (int iel = 0; iel < nel; iel++)
            for (int k = 0; k < nspec; k++) {
                int nn = neighbors[iel].length;
                diffusionEvents[iel][k] = new int[nn];
            }

        stimTab = getStimulationTable();
        stimtargets = this.getVolumeGrid().getAreaIndexes(stimTab.getTargetIDs());
        stimulationEvents = new int[nel][nspec];

        // workspace for the calculation
        wkA = new int[nel][nspec];
        wkB = new int[nel][nspec];

        double[][] regcon = getRegionConcentrations();
        double[][] regsd = getRegionSurfaceDensities();

        // apply initial conditions over the grid
        for (int i = 0; i < nel; i++) {
            double v = volumes[i];
            double[] rcs = regcon[eltregions[i]];

            for (int j = 0; j < nspec; j++)
                wkA[i][j] = wkB[i][j] = this.randomRound(v * rcs[j] * PARTICLES_PUVC);

            double a = surfaceAreas[i];
            double[] scs = regsd[eltregions[i]];
            if (a > 0 && scs != null) {
                for (int j = 0; j < nspec; j++)
                    if (Double.isNaN(scs[j])) {
                        // means not specified by the user;
                    } else
                        wkA[i][j] = wkB[i][j] = this.randomRound(a * scs[j] * PARTICLES_PUASD);
            }

            /*
             * if (i % 20 == 0) { E.info("elt " + i + " region " + eltregions[i]
             * + " n0 " + wkA[i][0]); }
             */
        }

        if (sdRun.initialStateFile != null) {
            if (this.resultWriters.size() == 0) {
                log.error("Unable to read state because writers are disabled");
                throw new RuntimeException("Unable to read state because writers are disabled");
            }

            int[][] cc = (int[][]) resultWriters.get(0).loadState(sdRun.initialStateFile, this);
            ArrayUtil.copy(cc, this.wkA);
            ArrayUtil.copy(cc, this.wkB);
        }

        dt = sdRun.fixedStepDt;
        lndt = Math.log(dt);

        // final things we need is something to generate particle numbers
        // for steps of given n, p
        if (useBinomial())
            interpSG = InterpolatingStepGenerator.getBinomialGenerator();
        else if (usePoisson()) {
            interpSG = InterpolatingStepGenerator.getPoissonGenerator();
        } else {
            E.error("unknown probability distribution");
        }

        if (doShared() || doParticle() || doIndependent()) {
            if (doShared()) {
                E.info("Using SHARED destination allocation");
            } else {
                E.info("Using PER PARTICLE destination allocation");
            }
            // FIXME: what about independent

            pSharedOut = new double[nel][nspec];
            fSharedExit = new double[nel][nspec][];

            int maxnn = 0;
            for (int iel = 0; iel < nel; iel++)
                for (int k = 0; k < nspec; k++) {
                    int nn = neighbors[iel].length;
                    fSharedExit[iel][k] = new double[nn];
                    if (nn > maxnn)
                        maxnn = nn;
                }
            log.info("max no of neighbors for a single element is {}", maxnn);

            for (int iel = 0; iel < nel; iel++) {
                for (int k = 0; k < nspec; k++) {

                    double ptot = 0.;
                    double[] pcnbr = new double[neighbors[iel].length];

                    for (int j = 0; j < pcnbr.length; j++) {
                        double lnpgo = lnfdiff[k] + lnCC[iel][j] + lndt - lnvolumes[iel];
                        // probability is dt * K_diff * contact_area /
                        // (center_to_center_distance * source_volume)
                        // gnbr contains the gometry: contact_area / distance

                        double p = Math.exp(lnpgo);
                        ptot += p;
                        pcnbr[j] = ptot;
                    }

                    if (ptot > 1/Math.E) { // why this value, who knows?
                        // WK 9 11 2007
                        System.out.println("WK===================================");
                        System.out.println("In DIFFUSION: probability TOO HIGH!");
                        System.out.println("Reduce your timestep, and try again...");
                        System.out.println("WK====================================");
                        System.exit(3);
                    }

                    pSharedOut[iel][k] = ptot;
                    for (int j = 0; j < pcnbr.length; j++)
                        fSharedExit[iel][k][j] = pcnbr[j] / ptot;
                }
            }
        }
    }

    protected int calculateNgo(String where, int n, double exp){
        final double g = random.gaussian(), r = random.random();
        final String msg;
        final int ngo;

        if (useBinomial()) {
            if (n * exp < NP) {
                ngo = StepGenerator.gaussianStep(n, exp, g, r, random.poisson(n * exp), NP);
                msg = "n*exp < " + NP;
            } else {
                ngo = StepGenerator.gaussianStep(n, exp, g, r);
                msg = "n*exp >= " + NP;
            }
        } else {
            ngo = StepGenerator.poissonStep(n, exp, g, r);
            msg = "not using binomial";
        }

        if (ngo >= 0)
            return ngo;

        log.warn("{} with {}: ngo is NEGATIVE (ngo={}, n={}, Math.exp(lnp)={})",
                 where, msg, ngo, n, exp);
        return 0;
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
                    // particles even the average entry per volume is less than
                    // one
                    // TODO - allow stim type (deterministic or poisson etc) in
                    // config;

                    int nk = stimtargets[i].length;
                    if (nk > 0) {
                        double as = astim[j] / nk;

                        for (int k = 0; k < nk; k++) {
                            int nin = this.randomRound(as);
                            int tgt = stimtargets[i][k];
                            ninjected += nin;
                            wkA[tgt][j] += nin;
                            this.stimulationEvents[tgt][j] += nin;
                        }
                    }
                }
            }
        }

        // initialize wkB to the current values.
        // It will hold the midstep values for the leapfrog, after diffusion
        // but before reactions.
        ArrayUtil.copy(wkA, wkB);

        // diffusion step;
        for (int iel = 0; iel < nel; iel++) {

            for (int k = 0; k < nspec; k++) {
                if (lnfdiff[k] > -90) { // FIXME: what is -90?

                    int np0 = wkA[iel][k];

                    if (np0 > 0) {

                        switch(algoID) {
                        case INDEPENDENT:
                        case SHARED:
                            parallelAndSharedDiffusionStep(iel, k);
                            break;
                        case PARTICLE:
                            particleDiffusionStep(iel, k);
                            break;

                        default:
                            assert false;
                        }
                    }
                }
            }
        }

        // for the reaction step, the source array is wkB and the
        // destination is wkA
        ArrayUtil.copy(wkB, wkA);

        // reaction step;
        for (int iel = 0; iel < nel; iel++) {
            // start and end quantities for each species in a single
            // volume
            int[] nstart = wkB[iel], nend = wkA[iel];

            for (int ireac = 0; ireac < rtab.getNReaction(); ireac++)
                reactionStep(nstart, nend, iel, ireac);
        }

        // now wkA contains the actual numbers again;
        return dt;
    }

    public int reactionStep_nwarn1, reactionStep_nwarn2;

    protected void reactionStep(int[] nstart, int[] nend, int iel, int ireac) {
        int[] ri = reactantIndices[ireac];
        int[] pi = productIndices[ireac];

        int[] rs = reactantStochiometry[ireac];
        int[] ps = productStochiometry[ireac];

        Object[] java_sucks = calculatePropensity(ri, pi, rs, ps,
                                                  reactantPowers[ireac],
                                                  lnrates[ireac], lnvolumes[iel],
                                                  nstart);
        double lnp = (Double)java_sucks[0];
        int ns = (Integer)java_sucks[1];

        lnp += lndt;

        if (lnp > 0) {
            if (++reactionStep_nwarn1 < 500)
                log.warn("p too large at element {} reaction {}: capping {} to 100%",
                         iel, ireac, Math.exp(lnp));
            lnp = 0;
        }

        if (ns > 0) {
            int ngo;
            final int b;
            if (ns == 1) {
                // TODO use table to get rid of exp
                ngo = (random.random() < Math.exp(lnp) ? 1 : 0);
                b = 1;
            } else if (ns <= StepGenerator.NMAX_STOCHASTIC) {
                ngo = interpSG.nGo(ns, lnp, random.random());
                b = 2;
            } else {
                ngo = this.calculateNgo("advance(reaction)", ns, Math.exp(lnp));
                b = 3;
            }

            if (rtab.getRates()[ireac] == 0 && ngo > 0)
                log.warn("ns={} -> ngo={} (lnp={}) b={}", ns, ngo, lnp, b);

            /* Update the new quantities in npn */

            int navail = nend[ri[0]] / rs[0];
            for (int k = 1; k < ri.length; k++) {
                int navail2 = nend[ri[k]] / rs[k];
                if (navail2 < navail)
                    navail = navail2;
            }

            if (ngo > navail) {
                /* TODO as for diffusion, we've got more particles going
                 * than there actually are. Should regenerate all
                 * reactions on this element
                 * or use a binomial to share them out
                 * or use a smaller timestep.
                 */
                if (++reactionStep_nwarn2 < 500)
                    log.warn("reaction {} ran out of particles - need {} but have {}",
                             ireac, ngo, navail);
                ngo = navail;
            }

            if (ngo > 0) {
                for (int k = 0; k < ri.length; k++) {
                    nend[ri[k]] -= ngo * rs[k];
                    if (nend[ri[k]] < 0) {
                        log.error("nend is negative: {}", nend);
                        log.info("reaction {}: ri={} pi={} rs={} ps={}",
                                 ireac, ri, pi, rs, ps);
                    }
                }

                for (int k = 0; k < pi.length; k++)
                    nend[pi[k]] += ngo * ps[k];

                this.reactionEvents[iel][ireac] += ngo;
            }
        }
    }

    // WK 8 28 2007
    private final void parallelAndSharedDiffusionStep(int iel, int k) {
        int np0 = wkA[iel][k];
        int inbr[] = neighbors[iel];
        double[] fshare = fSharedExit[iel][k];

        int ngo;

        if (np0 == 1) {
            ngo = (random.random() < pSharedOut[iel][k] ? 1 : 0);
        } else if (np0 < StepGenerator.NMAX_STOCHASTIC) {
            ngo = interpSG.nGo(np0, Math.log(pSharedOut[iel][k]), random.random());

            if (ngo < 0) {
                System.out.println("in parallelAndSharedDiffusionStep 1st else: ngo is NEGATIVE. Exiting...");
                System.exit(3);
            }
        } else {
            ngo = this.calculateNgo("parallelAndSharedDiffusionStep", np0, pSharedOut[iel][k]);
        }

        assert ngo >= 0;

        /* if (ngo < (# of neighbors)*SHARED_DIFF_PARTICLES) then do
         *    shared_diffusion
         * else
         *    do independent_diffusion
        */
        if (ngo <= (inbr.length) * SHARED_DIFF_PARTICLES) {
            /* SHARED diffusion */

            wkB[iel][k] -= ngo;
            for (int i = 0; i < ngo; i++) {
                double r = random.random();
                int io = 0;
                while (r > fshare[io])
                    io++;

                wkB[inbr[io]][k] ++;
                this.diffusionEvents[iel][k][io] ++;
            }
        } else {
            /* MULTINOMIAL diffusion */

            int ngo_remaining = ngo;  // the number of particles not yet diffused
            double prev = 0;
            for (int j = 0; j < inbr.length - 1; j++) {
                double pgoTmp = (fSharedExit[iel][k][j] - prev)/(fSharedExit[iel][k][inbr.length-1] - prev);
                double lnpgo;
                if (pgoTmp > 0.5 && ngo_remaining < StepGenerator.NMAX_STOCHASTIC)
                    lnpgo = Math.log(1.0 - pgoTmp);
                else
                    lnpgo = Math.log(pgoTmp);

                prev = fSharedExit[iel][k][j];

                //This next section uses the tables KTB
                if (ngo_remaining < StepGenerator.NMAX_STOCHASTIC) {
                    if (ngo_remaining == 1)
                        ngo = (random.random() < fSharedExit[iel][k][j] ? 1 : 0); //2011 BHK for ngo_remaining == 1
                    else if (ngo_remaining == 0) // 2011 BHK, occaisionally will run out of particles on 2nd to last neighbor
                        ngo = 0;
                    else {
                        if (pgoTmp <= 0.5)
                            ngo = interpSG.nGo(ngo_remaining, lnpgo, random.random()); // 2011 BHK
                        else
                            ngo = ngo_remaining - interpSG.nGo(ngo_remaining, lnpgo, random.random());
                    }
                } else if (ngo_remaining * Math.exp(lnpgo) < NP)
                    ngo = StepGenerator.gaussianStep(ngo_remaining, Math.exp(lnpgo), random.gaussian(), random.random(),
                                                     random.poisson(ngo_remaining * Math.exp(lnpgo)), NP);
                else
                    ngo = StepGenerator.gaussianStep(ngo_remaining, Math.exp(lnpgo), random.gaussian(), random.random());

                if (ngo < 0) {
                    ngo = 0;
                    System.out.println("parallelAndSharedDiffusionStep multinomial: ngo is NEGATIVE.");
                    System.out.println("ngo: " + ngo + " ngo_remaining: " + ngo_remaining + "pgoTmp " + pgoTmp);
                } else if (ngo > ngo_remaining) {
                    if (++nngowarn < 10)
                        log.warn("parallelAndSharedDiffusionStep multinomial: "
                                 + "ngo = {} > {} = ngo_remaining, setting ngo=ngo_remaining ",
                                 ngo, ngo_remaining);
                    ngo = ngo_remaining;
                }

                wkB[iel][k] -= ngo;
                wkB[inbr[j]][k] += ngo;
                this.diffusionEvents[iel][k][j] += ngo;
                ngo_remaining -= ngo;
            } //end of loop through all but last neighbor

            ngo = ngo_remaining;

            wkB[iel][k] -= ngo;
            wkB[inbr[inbr.length - 1]][k] += ngo;
            this.diffusionEvents[iel][k][inbr.length - 1] += ngo;

            if (wkB[iel][k] < 0)
                log.warn("parallelAndSharedDiffusionStep multinomial: wkB[iel][k] = {} is negative",
                         wkB[iel][k]);
        }
    }

    private final void particleDiffusionStep(int iel, int k) {
        int np0 = wkA[iel][k];
        int inbr[] = neighbors[iel];
        // int nnbr = inbr.length;
        double[] fshare = fSharedExit[iel][k];
        double ptot = pSharedOut[iel][k];

        for (int i = 0; i < np0; i++) {
            double r = random.random();

            if (r < ptot) {
                wkB[iel][k] -= 1; // ???
                double fr = r / ptot;
                int io = 0;
                while (fr > fshare[io]) {
                    io++;
                }
                wkB[inbr[io]][k] += 1;
                this.diffusionEvents[iel][k][io] ++;
            }
        }
    }

    @Override
    public int getGridPartNumb(int i, int j) {
        return wkA[i][j];
    }

    @Override
    public double getGridPartConc(int i, int j) {
        int val = getGridPartNumb(i, j);
        return val * NM_PER_PARTICLE_PUV / volumes[i];
    }
}
