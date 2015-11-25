package neurord.numeric.stochastic;


public class BinomialTable extends ProbabilityTable {

    static public final int NMAX = 140;

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
            double[] abv = ncmtbl[i-1];
            double[] row = ncmtbl[i] = new double[nel];
            row[0] = 1;
            for (int j = 1; j < nel; j++)
                row[j] = abv[j-1] + (j < abv.length ? abv[j] : abv[j-1]);
        }
    }

    public double ncm(int n, int m) {
        return ncmtbl[n][m > n-m ? n-m : m];
    }

    public long ncm_l(int n, int m) {
        return Math.round(ncm(n, m));
    }

    public void print(int n) {
        for (int i = 0; i < n; i++) {
            System.out.print("row " + i + "  ");

            for (int j = 0; j < i; j++)
                System.out.print("" + ncm_l(i, j) + " ");
            System.out.println();
        }
    }


    public static void main(String[] argv) {
        BinomialTable btbl = BinomialTable.getTable();
        btbl.print(20);
    }

}
