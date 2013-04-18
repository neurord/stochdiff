package org.textensor.stochdiff.numeric.stochastic;

import org.textensor.report.E;
import org.textensor.stochdiff.numeric.BaseCalc.distribution_t;
import static org.textensor.stochdiff.numeric.BaseCalc.distribution_t.*;

import static java.lang.String.format;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/*
 * Step generator for the case that the calculation may include
 * very many different transition probabilities so that the
 * DiscretePStepGenerator would require an unfeasibly large array
 * of transition count tables.
 *
 *
 */



public class InterpolatingStepGenerator extends StepGenerator {
    static final Logger log = LogManager.getLogger(InterpolatingStepGenerator.class);

    public final int NRANGES = 600;

    public final double lnpmin = Math.log(1.e-8);
    public final double lnpmax = -1.e-11;

    public final double deltalnp = (lnpmax-lnpmin) / NRANGES;

    private final NGoTable[][] pnTable;

    private static InterpolatingStepGenerator bInstance;
    private static InterpolatingStepGenerator pInstance;


    public static InterpolatingStepGenerator getBinomialGenerator() {
        if (bInstance == null)
            bInstance = new InterpolatingStepGenerator(BINOMIAL);

        return bInstance;
    }

    public static InterpolatingStepGenerator getPoissonGenerator() {
        if (pInstance == null)
            pInstance = new InterpolatingStepGenerator(POISSON);

        return pInstance;
    }

    private InterpolatingStepGenerator(distribution_t mode) {
        pnTable = new NGoTable[NRANGES+1][StepGenerator.NMAX_STOCHASTIC+1];

        log.info("Using {} step generator with {}×{} tables",
                 mode, pnTable.length, pnTable[0].length);

        for (int i = 0; i <= NRANGES; i++) {
            double lnp = lnpmin + i * deltalnp;
            for (int j = 2; j < pnTable[0].length; j++)
                pnTable[i][j] = new NGoTable(j, lnp, mode);
        }
    }

    @Override
    public String toString() {
        return String.format("%s.%s[%d·%d tables, %.3g:%.3g:%.3g]",
                             getClass().getSimpleName(),
                             pnTable[0][2].mode, pnTable.length, pnTable[0].length,
                             lnpmin, lnpmax, deltalnp);
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
        if (n > NMAX_STOCHASTIC)
            throw new RuntimeException("n too large (" + n + ")");

        double lnp2 = (lnp - lnpmin) / deltalnp;
        if (Double.isInfinite(lnp2))
            return 0;

        int ia = (int) lnp2;
        if (ia < 0)
            return 0;

        if (ia > pnTable.length - 2)
            ia = pnTable.length - 2;

        double f = (lnp - (lnpmin + ia * deltalnp)) / deltalnp;

        double rna = pnTable[ia][n].rnGo(r);
        double rnb = pnTable[ia+1][n].rnGo(r);
        int ngo = (int)(f * rnb + (1. - f) * rna);

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
