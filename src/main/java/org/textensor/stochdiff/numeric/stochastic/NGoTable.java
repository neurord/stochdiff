package org.textensor.stochdiff.numeric.stochastic;

import org.textensor.report.E;


public final class NGoTable {

    double lnp;
    int nparticle;

    double[] cprob; // cumulative probabilities
    int ncprob;

    public NGoTable(int n, double lnp0) {
        lnp = lnp0;
        nparticle = n;

        double p = Math.exp(lnp);
        double q = 1. - p;
        double lnq = Math.log(q);


        BinomialTable btab = BinomialTable.getTable();

        // pf is the full table - don't want the whole thing stored;
        double[] wk = new double[n+1];

        /*
         * accumulate c in reverse order: we add the small quantities first
         * so they don't get lost in rounding errors
         * (which is what would happen if we add them to
         * something of order 1)
         */
        double c = 0.;
        ncprob = -1;
        for (int i = n; i >= 0; i--) {
            double pn = Math.exp(i * lnp + (n-i) * lnq) * btab.ncm(n, i);
            c += pn;
            wk[i] = c;
            if (ncprob < 0 && c > 1.e-11) {
                ncprob = i;
            }
        }


        /* at this stage, wk[i] contains the probability that
         * i or more particles move in the step. wk[0] must be 1
         * unless something has gone wrong.
         */
        if (Math.abs(1. - c) > 1.e-14) {
            E.error("cumulative probability miscount? "+ c +
                    " for n, p, nkept " + n + " " + p + " " + ncprob);
        }

        /*
         * What we actually want is the cumulative probability of n or fewer
         * particles moving: cprob[0] is the probabiulioty of 0 moving
         * cprob[1] is the probability of either 0 or 1 moving etc
         */
        cprob = new double[ncprob+1];
        for (int i = 0; i < ncprob; i++) {
            cprob[i] = 1. - wk[i+1];
        }
        cprob[ncprob] = 2.;
        // one extra element on the end to save testing for the end condition;

    }


    /*
     * Step generation
     *
     * There are three methods here - no one method is good for all cases
     *
     * First is a simple-minded walk through the table until we overstep the
     * right box.
     *
     * The second is a binary search, which is suboptimal becasue the
     * cumulative distribution has a very long tail as it approaches 1 contianing
     * elements that will almost never be used.
     *
     * one could switch between the two according to p and the number of
     * particles, but on the assumption that p will be small if n is large
     * (otherwise, the timestep should be shorter...) the default is to
     * use the sequential lookup.
     *
     *
     * TODO The best thing is probaby to precompute an optimal search strategy
     * given the actual data (and maybe generate code for it)
     */



    public int nGoSeq(double r) {
        int ret = 0;
        while (cprob[ret] < r) {
            ret += 1;
        }
        return ret;
    }


    public int nGoBS(double r) {
        int bot = 0;
        int top = ncprob;

        while (top - bot > 1) {
            int ic = (bot + top) / 2;
            if (r < cprob[ic]) {
                top = ic;
            } else {
                bot = ic;
            }
        }
        return bot;
    }



    public int nGo(double r) {
        int ret = 0;
        while (cprob[ret] < r) {
            ret++;
        }
        return ret;
    }


    /*
     * rather than returning just the interval that the random number
     * lies in, we return a real from within the interval according to
     * the position of r across its domain.
     * this contains the nGo information since nGo(r) = (int)(rnGo(r))
     * and can also be used for interpolating between different
     * probabilities.
     */


    public double rnGo(double r) {
        double ret = 0.;
        if (r < cprob[0]) {
            ret = r / cprob[0];

        } else {
            int ia = 1;
            while (cprob[ia] < r) {
                ia++;
            }
            ret = ia + (r - cprob[ia-1]) / (cprob[ia] - cprob[ia-1]);
        }

        return ret;
    }







    /*
     * The rest is just for testing that the results are sensible
     */

    public void print() {
        StringBuffer sb = new StringBuffer();
        sb.append("n=" + nparticle + " p="+ Math.exp(lnp) +
                  " njmax=" + ncprob + "\n");

        for (int i = 0; i < ncprob; i++) {
            sb.append("P(n<=" + i + ")= ");
            sb.append(cprob[i] + "\n");
        }
        System.out.println(sb.toString());


        for (int i = 0; i < 10; i++) {
            double r = 0.001 + 0.99 * (i / 9.);
            System.out.println("" + i + " " + nGo(r) + " " + rnGo(r));
        }
    }





    private void lookupCheck() {
        for (int i = 0; i <= 50; i++) {
            double r = (1. * i) / 50.;
            int n1 = nGoSeq(r);
            int n2 = nGoBS(r);
            if (n1 != n2) {
                E.error("different results in lookup check " + nparticle + " " +
                        Math.exp(lnp) + " " + n1 + " " + n2);
            }
        }
    }




    private long lookupTimeSeq() {
        long t0 = System.currentTimeMillis();
        double rnx = 1.e7;
        int njt = 0;
        for (int i = 0; i < rnx; i++) {
            njt += nGoSeq(i / rnx);
        }
        long t1 = System.currentTimeMillis();
        return (t1 - t0);
    }

    private long lookupTimeBS() {
        long t0 = System.currentTimeMillis();
        double rnx = 1.e7;
        int njt = 0;
        for (int i = 0; i < rnx; i++) {
            njt += nGoBS(i / rnx);
        }
        long t1 = System.currentTimeMillis();
        return (t1 - t0);
    }

    private long lookupTimeSwitch() {
        long t0 = System.currentTimeMillis();
        double rnx = 1.e7;
        int njt = 0;
        for (int i = 0; i < rnx; i++) {
            njt += nGo(i / rnx);
        }
        long t1 = System.currentTimeMillis();
        return (t1 - t0);
    }



    private static void lookupTest() {
        long tstot = 0;
        long tbtot = 0;
        long tctot = 0;

        for (int i = 10; i <= 90; i+= 10) {
            for (int ip = -6; ip < -1; ip++) {
                double lp = 2. * ip;
                NGoTable jc = new NGoTable(i, lp);

                jc.lookupCheck();

                long ts = jc.lookupTimeSeq();
                long tb = jc.lookupTimeBS();
                long tc = jc.lookupTimeSwitch();

                tstot += ts;
                tbtot += tb;
                tctot += tc;
                double p = Math.exp(lp);
                System.out.println("timings " + i + " " + p + " " +
                                   ts + " " + tb + " " + tc);

            }
        }

        E.info("seq " + tstot + "   bs " + tbtot + " " + tctot);
    }


    private static void dumpExamples() {
        new NGoTable(10, Math.log(0.1)).print();
        new NGoTable(90, Math.log(0.1)).print();
        new NGoTable(10, Math.log(0.01)).print();
        new NGoTable(90, Math.log(0.01)).print();
        new NGoTable(10, Math.log(0.001)).print();
        new NGoTable(90, Math.log(0.001)).print();
        new NGoTable(10, Math.log(0.0001)).print();
        new NGoTable(90, Math.log(0.0001)).print();
    }


    public static void main(String[] argv) {
        dumpExamples();
        //   lookupTest();
    }


}
