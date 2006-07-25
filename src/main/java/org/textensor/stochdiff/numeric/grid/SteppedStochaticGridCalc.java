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

    public static final double PARTICLES_PUVC = 0.6022;
    // particles Per Unit Volume and Concentration
    public final static double CONC_OF_N = 1. / 0.6022;


    Column mconc;

    ReactionTable rtab;
    VolumeGrid vgrid;

    StimulationTable stimTab;

    double dt;

    int nel;
    int nspec;
    double[] volumes;
    double[] lnvolumes;
    double[] fdiff;
    double[] lnfdiff;

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

    double[] rates;
    double[] lnrates;

    int[] stimtargets;


    double[] intlogs;
    double lndt;




    InterpolatingStepGenerator interpSG;

    MersenneTwister random;


    int nwarn;

    public SteppedStochaticGridCalc(SDRun sdm) {
        super(sdm);
    }


    public final void init() {
        // something to generate the random nunmbers
        random = new MersenneTwister();

        rtab = getReactionTable();

        nreaction = rtab.getNReaction();
        rates = rtab.getRates();
        lnrates = ArrayUtil.log(rates);

        reactantIndices = rtab.getReactantIndices();
        productIndices = rtab.getProductIndices();



        vgrid = getVolumeGrid();

        nel = vgrid.getNElements();
        nspec = rtab.getNSpecies();
        volumes = vgrid.getElementVolumes();
        lnvolumes = ArrayUtil.log(volumes);

        fdiff = rtab.getDiffusionConstants();
        lnfdiff = ArrayUtil.log(fdiff);

        neighbors = vgrid.getPerElementNeighbors();
        couplingConstants = vgrid.getPerElementCouplingConstants();
        lnCC = ArrayUtil.log(couplingConstants);

        stimTab = getStimulationTable();
        stimtargets = vgrid.getElementIndexes(stimTab.getTargetIDs());


        // workspace for the calculation
        wkA = new int[nel][nspec];
        wkB = new int[nel][nspec];
        wkReac = new int[nreaction];


        // apply initial conditions over the grid
        double[] c0 = getNanoMolarConcentrations();
        for (int i = 0; i < nel; i++) {
            double v = volumes[i];
            for (int j = 0; j < nspec; j++) {
                double rnp = v * c0[j] * PARTICLES_PUVC;
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
        interpSG = InterpolatingStepGenerator.getGenerator();


    }

    @SuppressWarnings("boxing")
    private String getGridConcsText(double time) {
        StringBuffer sb = new StringBuffer();
        // TODO tag specific to integer quantities;
        sb.append("gridConcentrations " + nel + " " + nspec + " " + time + "\n");
        for (int i = 0; i < nel; i++) {
            for (int j = 0; j < nspec; j++) {
                sb.append(" " + (CONC_OF_N * wkB[i][j] / volumes[i]));
            }
            sb.append("\n");
        }
        return sb.toString();
    }



    public final void run() {
        init();

        if (resultWriter != null) {
            resultWriter.writeString(vgrid.getAsText());
        }

        double time = 0.;
        double runtime = sdRun.runtime;

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



    // NB the following method is one of the only two that need optimizing
    // (the other is nGo in the interpolating step generator)
    // things to do (in the c version)
    //  - use BLAS calls for array operations,
    //  - remove the two remaining exps
    //  - unwrap inner conditionals for different reaction types
    //  - make nGo inlinable


    public double advance(double tnow) {

        // add in any injections
        double[][] stims = stimTab.getStimsForInterval(tnow, dt);
        for (int i = 0; i < stims.length; i++) {
            double[] astim = stims[i];
            for (int j = 0; j < astim.length; j++) {
                if (astim[j] > 0.) {
                    /*
                     E.info("non zero stim for elt " + j + " " +
                           stimtargets[i] + " " + astim[j] + " at time " + tnow);
                     */

                    wkA[stimtargets[i]][j] += (int)(astim[j] + 0.5);
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

            int inbr[] = neighbors[iel];
            double lngnbr[] = lnCC[iel];
            int nnbr = inbr.length;

            for (int k = 0; k < nspec; k++) {
                int np0 = wkA[iel][k];

                if (np0 > 0) {
                    for (int j = 0; j < nnbr; j++) {
                        // use logs here so the operations are all additions
                        // and the compiler should be able to be clever

                        double lnpgo = lnfdiff[k] + lngnbr[j] + lndt - lnvolumes[iel];
                        // probability is  dt * K_diff * contact_area /
                        //    (center_to_center_distance * source_volume)
                        // gnbr contains the gometry:  contact_area / distance

                        if (lnpgo > -1.) {
                            if (nwarn < 5) {
                                E.warning("p too large at element " + iel + " transition " + j + " to  " + inbr[j] +
                                          " - capping " + Math.exp(lnpgo) +
                                          " coupling is " + lngnbr[j]);
                                nwarn++;
                            }
                            lnpgo = -1.;
                        }


                        int ngo = 0;
                        if (np0 == 1) {
                            // TODO - use table anyway - avoid exp!
                            ngo = (random.random() < Math.exp(lnpgo) ? 1 : 0);

                        } else {
                            ngo = interpSG.nGo(np0, lnpgo, random.random());

                        }


                        if (ngo > wkB[iel][k]) {
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



                if (n <= 0) {

                } else {
                    int ngo = 0;
                    if (n == 1) {
                        // TODO use table to get rid of exp
                        ngo = (random.random() < Math.exp(lnp) ? 1 : 0);
                    } else {
                        ngo = interpSG.nGo(n, lnp, random.random());
                    }

                    // update the new quantities in npn;
                    int navail = nend[ri[0]];
                    if (ri[1] >= 0 && navail < nend[ri[1]]) {
                        navail = nend[ri[1]];
                    }
                    if (ngo > navail) {
                        ngo = navail;
                        // TODO as for diffusion, we've got more particles going
                        // than there actually are. Should regenerate all
                        // reactions on theis element
                        // or use a binomial to share them out
                    }


                    nend[ri[0]] -= ngo;
                    if (ri[1] >= 0) {
                        nend[ri[1]] -= ngo;
                    }
                    nend[pi[0]] += ngo;
                    if (pi[1] >= 0) {
                        nend[pi[1]] += ngo;
                    }

                    // TODO this "if (ri[1] >= 0)" business is not great
                    // it applies for the A+B->C case, where there is a
                    // second reactant. We could probably do better by
                    // unrolling the four cases into separate blocks according
                    // to the reaction type
                }
            }
        }

        // now wkA contains the actual numbers again;
        return dt;
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


}
