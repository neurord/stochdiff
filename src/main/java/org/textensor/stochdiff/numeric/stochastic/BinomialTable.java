package org.textensor.stochdiff.numeric.stochastic;


public class BinomialTable {

    private final static int NMAX = 150;

    private static BinomialTable instance;


    // can use longs up to about 60, but not further
    private double[][] ncmtbl;


    // Singleton - only ever one instance
    public static BinomialTable getTable() {
        if (instance == null) {
            instance = new BinomialTable();
        }
        return instance;
    }




    private BinomialTable() {
        ncmtbl = new double[NMAX][];
        ncmtbl[0] = new double[0];
        ncmtbl[1] = new double[1];
        ncmtbl[1][0] = 1;

        for (int i = 2; i < NMAX; i++) {
            int nel = i/2 + 1;
            double[] row = new double[nel];
            double[] abv = ncmtbl[i-1];
            row[0] = 1;
            for (int j = 1; j < nel; j++) {
                row[j] = abv[j-1] + (j < abv.length ? abv[j] : abv[j-1]);
            }
            ncmtbl[i] = row;
        }
    }




    public double ncm(int n, int m) {
        double ret = 0;
        if (m > n-m) {
            ret = ncmtbl[n][n-m];
        } else {
            ret = ncmtbl[n][m];
        }
        return ret;
    }



    public void print(int n) {
        for (int i = 0; i < n; i++) {
            StringBuffer sb = new StringBuffer();
            sb.append("row " + i + "  ");
            double[] c = ncmtbl[i];
            for (int j = 0; j < c.length; j++) {
                sb.append(c[j]);
                sb.append(" ");
            }
            System.out.println(sb);
        }
    }


    public static void main(String[] argv) {
        BinomialTable btbl = BinomialTable.getTable();
        btbl.print(20);
    }

}
