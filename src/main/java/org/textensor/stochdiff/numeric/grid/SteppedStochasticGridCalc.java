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
import java.util.Collection;

import org.textensor.stochdiff.model.SDRun;
import org.textensor.stochdiff.numeric.BaseCalc;
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

public class SteppedStochasticGridCalc extends StochasticGridCalc {
    static final Logger log = LogManager.getLogger(SteppedStochasticGridCalc.class);

    // WK 8 28 2007
    // in parallelAndSharedDiffusionStep(),
    // (1) if the number of molecules to duffuse is less than
    // SHARED_DIFF_PARTICLES*(number of neighboring subvolume elements),
    // we do shared diffusion,
    // (2) otherwise, we do parallel diffusion.
    public static final int SHARED_DIFF_PARTICLES = 4;

    double[] lnfdiff;

    double[][] lnCC;

    int[][] wkB;

    double[] diffusionConstants;

    int[][] reactantIndices;
    int[][] productIndices;

    int[][] reactantStoichiometry;
    int[][] productStoichiometry;

    /**
     * Propensity powers for true second- and higher-order reactions.
     */
    int[][] reactantPowers;

    double[] lnvolumes;
    double[] lnrates;

    double lndt;

    InterpolatingStepGenerator stepper;
    int nngowarn = 0;         //added in v2.1.1 by BHK to keep track of a different type of warning

    double[][] pSharedOut;
    double[][][] fSharedExit;

    long event_count = 0;

    public SteppedStochasticGridCalc(int trial, SDRun sdm) {
        super(trial, sdm);
    }

    @Override
    public final void init() {
        super.init();

        lnrates = ArrayUtil.log(rtab.getRates());
        log.debug("lnrates: {}", lnrates);

        reactantIndices = rtab.getReactantIndices();
        productIndices = rtab.getProductIndices();

        reactantStoichiometry = rtab.getReactantStoichiometry();
        productStoichiometry = rtab.getProductStoichiometry();
        reactantPowers = rtab.getReactantPowers();

        lnvolumes = ArrayUtil.log(volumes);
        log.debug("lnvolumes: {}", lnvolumes);

        lnfdiff = ArrayUtil.log(fdiff);
        lnCC = ArrayUtil.log(couplingConstants);

        // workspace for the calculation
        wkB = new int[nel][nspec];
        ArrayUtil.copy(wkA, wkB);

        String statefile = this.sdRun.initialStateFile;
        if (statefile != null) {
            if (this.resultWriters.size() == 0) {
                log.error("Unable to read state because writers are disabled");
                throw new RuntimeException("Unable to read state because writers are disabled");
            }

            int[][] cc = (int[][]) resultWriters.get(0).loadState(statefile, this);
            ArrayUtil.copy(cc, this.wkA);
            ArrayUtil.copy(cc, this.wkB);
        }

        lndt = Math.log(dt);

        // final things we need is something to generate particle numbers
        // for steps of given n, p
        stepper = new InterpolatingStepGenerator(distID, random);

        log.info("Using {} destination allocation", algoID);
        // FIXME: is independent supported

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

    // NB the following method is one of the only two that need optimizing
    // (the other is nGo in the interpolating step generator)
    // things to do (in the c version)
    // - use BLAS calls for array operations,
    // - remove the two remaining exps
    // - unwrap inner conditionals for different reaction types
    // - make nGo inlinable

    @Override
    public double advance(double tnow, double tend) {
        // add in any injections
        double[][] stims = this.sdRun.getStimulationTable().getStimsForInterval(tnow, dt);
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

                    int[][] stimtargets = this.sdRun.getStimulationTargets();
                    int nk = stimtargets[i].length;
                    if (nk > 0) {
                        double as = astim[j] / nk;

                        for (int k = 0; k < nk; k++) {
                            int nin = this.randomRound(as);
                            int tgt = stimtargets[i][k];
                            wkA[tgt][j] += nin;
                            if (this.stimulationEvents != null)
                                this.stimulationEvents[tgt][j] += nin;
                        }
                    }

                    this.event_count ++;
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

                    this.event_count ++;
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

            for (int ireac = 0; ireac < rtab.getNReaction(); ireac++) {
                reactionStep(nstart, nend, iel, ireac);
                this.event_count ++;
            }
        }

        // now wkA contains the actual numbers again;
        if ((tend - tnow) - dt > 0.01 * dt)
            log.warn("Step {} is different than dt={}", tend - tnow, dt);

        return dt;
    }

    @Override
    protected long eventCount() {
        return this.event_count;
    }

    private int reactionStep_nwarn1, reactionStep_nwarn2;

    protected void reactionStep(int[] nstart, int[] nend, int iel, int ireac) {
        int[] ri = reactantIndices[ireac];
        int[] pi = productIndices[ireac];

        int[] rs = reactantStoichiometry[ireac];
        int[] ps = productStoichiometry[ireac];

        Object[] java_sucks = calculatePropensity(ri, pi, rs, ps,
                                                  reactantPowers[ireac],
                                                  lnrates[ireac], lnvolumes[iel],
                                                  nstart);
        double lnp = (Double)java_sucks[0];
        int n = (Integer)java_sucks[1];

        lnp += lndt;

        if (lnp > 0) {
            if (++reactionStep_nwarn1 < 500)
                log.warn("p too large at element {} reaction {}: capping {} to 100%",
                         iel, ireac, Math.exp(lnp));
            lnp = 0;
        }

        if (n > 0) {
            int ngo = this.stepper.versatile_ngo("advance(reaction)", n, Math.exp(lnp));

            if (rtab.getRates()[ireac] == 0 && ngo > 0)
                log.warn("n={} -> ngo={} (lnp={})", n, ngo, lnp);

            /* Update the new quantities in npn */

            if (ri.length > 0) {
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

                if (this.reactionEvents != null)
                    this.reactionEvents[iel][ireac] += ngo;
            }
        }
    }

    // WK 8 28 2007
    private final void parallelAndSharedDiffusionStep(int iel, int k) {
        int np0 = wkA[iel][k];
        int inbr[] = neighbors[iel];
        double[] fshare = fSharedExit[iel][k];

        int ngo = this.stepper.versatile_ngo("parallelAndSharedDiffusionStep", np0, pSharedOut[iel][k]);
        assert ngo >= 0;

        /* if (ngo < (# of neighbors)*SHARED_DIFF_PARTICLES) then do
         *    shared_diffusion
         * else
         *    do independent_diffusion
        */
        if (ngo <= inbr.length * SHARED_DIFF_PARTICLES) {
            /* SHARED diffusion */

            wkB[iel][k] -= ngo;
            for (int i = 0; i < ngo; i++) {
                double r = random.random();
                int io = 0;
                while (r > fshare[io])
                    io++;

                wkB[inbr[io]][k] ++;
                if (this.diffusionEvents != null)
                    this.diffusionEvents[iel][k][io] ++;
            }
        } else {
            /* MULTINOMIAL diffusion */

            double prev = 0;
            for (int j = 0; j < inbr.length - 1; j++) {
                double pgoTmp = (fSharedExit[iel][k][j] - prev)
                              / (fSharedExit[iel][k][inbr.length-1] - prev);
                prev = fSharedExit[iel][k][j];

                int ngo2 = stepper.versatile_ngo("parallelAndSharedDiffusionStep multinomial",
                                                 ngo, pgoTmp);

                assert ngo2 >= 0;

                if (ngo2 > ngo) {
                    if (++nngowarn < 10)
                        log.warn("parallelAndSharedDiffusionStep multinomial: "
                                 + "ngo2 = {} > {} = ngo, setting ngo2=ngo ",
                                 ngo2, ngo);
                    ngo2 = ngo;
                }

                wkB[iel][k] -= ngo2;
                wkB[inbr[j]][k] += ngo2;
                if (this.diffusionEvents != null)
                    this.diffusionEvents[iel][k][j] += ngo2;
                ngo -= ngo2;
            } //end of loop through all but last neighbor

            wkB[iel][k] -= ngo;
            wkB[inbr[inbr.length - 1]][k] += ngo;
            if (this.diffusionEvents != null)
                this.diffusionEvents[iel][k][inbr.length - 1] += ngo;

            if (wkB[iel][k] < 0)
                log.warn("parallelAndSharedDiffusionStep multinomial: wkB[iel][k] = {} is negative",
                         wkB[iel][k]);
        }
    }

    private final void particleDiffusionStep(int iel, int k) {
        int np0 = wkA[iel][k];
        int inbr[] = neighbors[iel];
        double[] fshare = fSharedExit[iel][k];
        double ptot = pSharedOut[iel][k];

        for (int i = 0; i < np0; i++) {
            double r = random.random();

            if (r < ptot) {
                wkB[iel][k] -= 1;
                double fr = r / ptot;
                int io = 0;
                while (fr > fshare[io])
                    io++;

                wkB[inbr[io]][k] += 1;
                if (this.diffusionEvents != null)
                    this.diffusionEvents[iel][k][io] ++;
            }
        }
    }

    /* Total number of possible reactions is the smallest number of
     * particles divided by stoichiometry.
     *
     * @returns propensity and maximum reaction extent
     */
    public static Object[] calculatePropensity(int[] ri, int[] pi,
                                               int[] rs, int[] ps,
                                               int[] rp,
                                               double lnrate, double lnvol,
                                               int[] nstart) {
        double lnp = lnrate + lnvol;
        int ns = Integer.MAX_VALUE;

        for (int k = 0; k < ri.length; k++) {
            int n = nstart[ri[k]];
            int p = rp[k];
            int nks = n / rs[k];

            if (nks < ns)
                ns = nks;

            if (p >= 1)
                /* FIXME: use falling factorial */
                lnp += p * (intlog(n) - lnvol - LN_PARTICLES_PUVC);
        }

        if (ns > 0)
            /* Apply a kludge so the stepper can generate a proper random number */
            lnp -= intlog(ns);

        return new Object[]{lnp, ns};
    }

    public Collection<IGridCalc.Event> getEvents() {
        return null;
    }
    public Collection<IGridCalc.Happening> getHappenings() {
        return null;
    }
}
