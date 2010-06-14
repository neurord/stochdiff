package org.textensor.stochdiff.inter;

import org.textensor.report.E;

public class SDState {

    public int nel;
    public int nspec;
    public String[] specids;

    public double[][] conc;

    public double[][] getData() {
        return conc;
    }


    public double[] getConc1() {
        int ne = conc.length;
        int ns = conc[0].length;
        double[] ret = new double[ne * ns];
        for (int i = 0; i < ne; i++) {
            for (int j = 0; j < ns; j++) {
                ret[ns * i + j] = conc[i][j];
            }
        }
        return ret;
    }

    public double[][] getConc2() {
        int ne = conc.length;
        int ns = conc[0].length;
        double[][] ret = new double[ne][ns];
        for (int i = 0; i < ne; i++) {
            for (int j = 0; j < ns; j++) {
                ret[i][j] = conc[i][j];
            }
        }
        return ret;
    }

}
