//5 20 2007: WK changed deltalnp's value to 0.03 from 0.3 to narrow the
//             probability interpolation intervals (and thus increased
//             the probability table size)
//written by Robert Cannon
package org.textensor.stochdiff.numeric.stochastic;

import org.textensor.report.E;




/*
 * Step generator for the case that the calculation may include
 * very many different transition probabilities so that the
 * DiscretePStepGenerator would require an unfeasibly large array
 * of transition count tables.
 *
 *
 */



public class InterpolatingStepGenerator extends StepGenerator {

    public final double lnpmin = Math.log(1.e-8);
    public final double lnpmax = Math.log(0.5);

    //<--WK
    public final static double deltalnp = 0.03;  //0.3 was the original value;
    //WK-->


    NGoTable[][] pnTable;

    int nppts;

    private static InterpolatingStepGenerator bInstance;
    private static InterpolatingStepGenerator pInstance;



    public static InterpolatingStepGenerator getBinomialGenerator() {
        if (bInstance == null) {
            bInstance = new InterpolatingStepGenerator(BINOMIAL);
        }
        return bInstance;
    }

    public static InterpolatingStepGenerator getPoissonGenerator() {
        if (pInstance == null) {
            pInstance = new InterpolatingStepGenerator(POISSON);
        }
        return pInstance;
    }

    public InterpolatingStepGenerator(int mode) {

        if (mode == BINOMIAL) {
            E.info("Using a BINOMIAL step generator");
        } else {
            E.info("Using a POISSON step generator");
        }

        nppts = (int)((lnpmax - lnpmin) / deltalnp + 2);
        pnTable = new NGoTable[nppts+1][StepGenerator.NMAX_STOCHASTIC+1];

        fullInit(mode);
    }


    // initialize all the tables - may not all be used ever -
    // could evaluate them as required
    private void fullInit(int mode) {
        for (int i = 0; i <= nppts; i++) {
            double lnp = lnpmin + i * deltalnp;
            for (int j = 2; j <= StepGenerator.NMAX_STOCHASTIC; j++) {
                pnTable[i][j] = new NGoTable(j, lnp, mode);
            }
        }
    }


    /*
     * TODO - this is by no means optimal. It shouldn't cost much
     * more than a single nGo call
     * Better - should get nGo from pnTable[ia][n] along with
     * delta_r and then do a test in pnTable[ia+1][n] using
     * these values to see if should use the same n or a higher one.
     *
     *   Could also use non-linear delta ln p to make sure delta_n is
     *   never more than 1.
     */

    public int nGo(int n, double lnp, double r) {
        int ia = (int)((lnp - lnpmin) / deltalnp);
        double f = (lnp - (lnpmin + ia * deltalnp)) / deltalnp;
        if (ia < 0) {
            ia = 0;
            f = 0.;
        }
        int ngo = 0;

        if (n > NMAX_STOCHASTIC) {
            E.error("n too large");



        } else {
            if (ia+1 > nppts) {
                E.error("ia too big " + n + " " + lnp + " " + r);
            }

            double rna = pnTable[ia][n].rnGo(r);
            double rnb = pnTable[ia+1][n].rnGo(r);
            ngo = (int)(f * rnb + (1. - f) * rna);

            //if (ngo != 0)
            {
                //    System.out.println(n + " " + lnp + " " + ngo);
                // System.out.println(ia + " " + lnp + " " + (lnpmin + ia*deltalnp) + " " + (lnpmin + (ia+1)*deltalnp));
            }
        }


        return ngo;
    }



    public void timeTest() {
        for (int n = 10; n <= 90; n += 20) {
            for (int ip = -6; ip <= -1; ip++) {
                double lnp = 1. * ip;
                long t0 = System.currentTimeMillis();
                double rnx = 1.e7;
                int njt = 0;
                for (int k = 0; k < rnx; k++) {
                    njt += nGo(n, lnp, k / rnx);
                }
                long t1 = System.currentTimeMillis();

                System.out.println("timings " + n + " " + lnp + " " +
                                   (t1 - t0));


                for (int k = 0; k <= 5; k++) {
                    double r = 0.001 + 0.99 * (k / 5.);
                    int ia = (int)((lnp - lnpmin) / deltalnp);
                    if (ia < 0) {
                        ia = 0;
                    }
                    int ina = pnTable[ia][n].nGo(r);
                    double rna = pnTable[ia][n].rnGo(r);
                    double rnb = pnTable[ia+1][n].rnGo(r);

                    System.out.println(" lnp, n, deltango " +
                                       lnp + " " + n + " " + ina + " " +
                                       (rnb - rna));


                }
            }
        }
    }

    public void dumpTable(int n, double lnp) {
        // TODO Auto-generated method stub
        int ia = (int)((lnp - lnpmin) / deltalnp);
        double f = (lnp - (lnpmin + ia * deltalnp)) / deltalnp;
        if (ia < 0) {
            ia = 0;
            f = 0.;
        }
        E.info("interpolationg between tables " + ia + " and " + (ia+1) + " factor " + f);
        pnTable[ia][n].print();
        pnTable[ia+1][n].print();
    }




}
