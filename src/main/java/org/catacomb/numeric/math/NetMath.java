

package org.catacomb.numeric.math;


public final class NetMath {




    public static double[] makeSampledBiExponential(double trin, double tfin,
            double dt) {
        double tf = tfin;
        double tr = trin;
        double eps = 1.e-6;
        if (tr < eps) {
            tr = eps;
        }
        if (tf < tr + eps) {
            tf = tr + eps;
        }
        double tmax = Math.log(tf/tr) / (1./tr - 1./tf);
        return (makeSampledBiExponential(tr, tf, 3 * tmax, dt));
    }


    public static double[] makeSampledBiExponential(double trin, double tfin,
            double ttot, double dt) {
        double tf = tfin;
        double tr = trin;
        double eps = 1.e-6;
        if (tr < eps) {
            tr = eps;
        }
        if (tf < tr + eps) {
            tf = tr + eps;
        }

        double tmax = Math.log(tf/tr) / (1./tr - 1./tf);
        double afac = 1./(Math.exp(-tmax / tf) - Math.exp(-tmax / tr));

        int np = (int)(ttot / dt);
        double[] dd = new double[np];

        for (int i = 0; i < np; i++) {
            double t = i * dt;
            dd[i] = afac * (Math.exp(-t / tf) - Math.exp(-t / tr));
        }
        return dd;
    }





    public static double[] vectorScale(int[] ii, double f) {
        int n = ii.length;
        double[] dd = new double[n];
        for (int i = 0; i < n; i++) dd[i] = f * ii[i];
        return dd;
    }















}




