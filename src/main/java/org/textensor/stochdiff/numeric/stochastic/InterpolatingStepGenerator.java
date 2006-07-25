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

    public final static double deltalnp = 0.3;


    NGoTable[][] pnTable;

    int nppts;

    private static InterpolatingStepGenerator instance;


    public static InterpolatingStepGenerator getGenerator() {
        if (instance == null) {
            instance = new InterpolatingStepGenerator();
        }
        return instance;
    }



    public InterpolatingStepGenerator() {

        nppts = (int)((lnpmax - lnpmin) / deltalnp + 2);

        pnTable = new NGoTable[nppts+1][StepGenerator.NMAX_STOCHASTIC+1];

        fullInit();
    }


    // initialize all the tables - may not all be used ever -
    // could evaluate them as required
    private void fullInit() {
        for (int i = 0; i <= nppts; i++) {
            double lnp = lnpmin + i * deltalnp;
            for (int j = 2; j <= StepGenerator.NMAX_STOCHASTIC; j++) {
                pnTable[i][j] = new NGoTable(j, lnp);
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
            // TODO avoid this exp
            ngo = (int)(Math.exp(lnp) * n + 0.5);

        } else {
            double rna = pnTable[ia][n].rnGo(r);
            double rnb = pnTable[ia+1][n].rnGo(r);
            ngo = (int)(f * rnb + (1. - f) * rna + 0.5);
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




}
