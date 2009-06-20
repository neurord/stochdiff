
package org.catacomb.numeric.math;



public abstract class DiagonalBlockMatrix {

    private final static double abs(double d) {
        return Math.abs(d);
    }


    private final static double max(double a, double b) {
        return (a > b ? a : b);
    }


    public static void main(String[] argv) {
        dbmTest();
    }

    public static void Sp(String s) {
        System.out.println(s);
    }


    public static final void dbmTest() {
        /*
          on laptop with nv = 6, nm=100, ne=1 takes about 95 ms,
          compared to 25 ms, for g77 version,  7 ms for g77 -O version.

         */

        int nv = 8;
        int nm = 99;
        int ne = 1;
        double[][][] a = new double[nm][3*nv+ne+1][nv+4];
        double[][][] b = new double[nm][3*nv+ne+1][nv+4];
        int[] nrpb = new int[nm];

        int ii = 1234;

        for (int i = 0; i < nm; i++) {
            for (int j = 0; j < nv+4; j++) {
                for (int k = 0; k < 3*nv+ne+1; k++) {
                    ii = 7 * ii;
                    ii = ii % 14567;
                    double ddd = 0.00345 * ii;
                    a[i][k][j] = ddd; // Math.random();
                    b[i][k][j] = a[i][k][j];
                }
            }
            nrpb[i] = nv;
        }

        //    nrpb[nm-1] += ne;


        nrpb[0] -= 2;
        nrpb[nm-1] += 3;


        long ttt = System.currentTimeMillis();
        double[] corr = dbmSolve(nv, nm, ne, nrpb, a);
        ttt = System.currentTimeMillis() - ttt;

        double maxdev = 0.0;

        for (int i = 0; i < nm; i++) {
            int k0 = i-1;
            if (k0 < 0) {
                k0 = 0;
            }
            int kk = 3;
            if (i <= 0 || i == nm-1) {
                kk = 2;
            }

            for (int j = 0; j < nrpb[i]; j++) {
                double v = 0.0;
                for (int k = 0; k < kk * nv; k++) {
                    v += b[i][k][j] * corr[nv*k0 + k];
                }
                for (int k = 0; k < ne; k++) {
                    v += b[i][kk*nv+k][j] * corr[nv*nm+k];
                }
                double dev = v - b[i][kk*nv+ne][j];
                dev = Math.abs(dev);
                if (dev > maxdev) {
                    maxdev = dev;
                }
                if (dev > 1.e-3) {
                    Sp("  " + i + " " + j + " " + v + " " +
                       b[i][kk*nv+ne][j]);
                }
            }
        }

        if (maxdev > 0.01) {
            int nl = corr.length;
            Sp("corr elts " + corr[nl-1] + " " + corr[nl-2]);

        }

        Sp("max deviation " + maxdev);
        Sp(" calc time " + ttt);
    }






    public static final double[] dbmSolve(int nm, int nv, int ne,
                                          int[] nrpb, double[][][] a) {

        int[] nnz = new int[nm];
        double[] corr = new double[nv * nm + ne];


        int ncollx = 3 * nv + ne + 1;

        int[] nelim = new int[nm];
        nelim[0] = 0;
        nelim[1] = nrpb[0];
        for (int k = 2; k < nm; k++) {
            nelim[k] = nelim[k-1] + nrpb[k-1] - nv;
        }

        /*
        for (int k = 1; k < nm; k++) {
          if (nelim[k] > nrpb[k-1]) {
        S.p ("too few rows in block above for elim");
        U.dumpArray ("nrpb ", nrpb);
        U.dumpArray ("nelim ", nelim);
          }
        }
        */

        // blocks 1 to nm-1
        for (int k = 0; k < nm; k++) {
            int ncoll = ncollx;
            if (k == 0 || k == nm-1) {
                ncoll -= nv;
            }
            double[][] b = a[k];
            int nel = nelim[k];

            // eliminate nel columns from this block using the block above;
            if (nel > 0) {
                double[][] s = a[k-1];
                int novlp = nnz[k-1] - ne - 1;
                int nu = nrpb[k-1] - nel;
                int ns = nnz[k-1];

                for (int ic = 0; ic < nel; ic++) {
                    for (int ir = 0; ir < nrpb[k]; ir++) {
                        double f = b[ic][ir];
                        for (int j = 0; j < novlp; j++) {
                            b[j+nel][ir] -= f * s[j][nu+ic];
                        }
                        // do the same to 'e-value' columns and r.h.s. vector
                        for (int j = 1; j < ne+2; j++) {
                            b[ncoll-j][ir] -= f *s[ns-j][nu+ic];
                        }
                    }
                }
            }

            for (int ic = 0; ic < ncoll-nel; ic++) {
                b[ic] = b[nel+ic];
            }
            diag(nrpb[k], ncoll-nel, a[k], k);
            nnz[k] = ncoll - nelim[k] - nrpb[k];
            for (int ic = 0; ic < nnz[k]; ic++) {
                b[ic] = b[ic + nrpb[k]];
            }
            for (int ic = nnz[k]; ic < ncoll; ic++) {
                b[ic] = null;
            }
        }

        if (nnz[nm-1] != 1) {
            Sp("solve error " + nnz[nm]);
        }


        // backsubstitution
        int l = nv * nm + ne;
        for (int k = nm-1; k >= 0; k--) {
            double[][] b = a[k];
            int nz = nnz[k];
            int nr = nrpb[k];
            for (int i = 1; i <= nr; i++) {
                double c = b[nz-1][nr-i];

                // terms involving e-values
                for (int j = 1; j <= ne && j < nz; j++) {
                    c -= b[nz-j-1][nr-i] * corr[nv*nm+ne-j];
                }
                for (int j = 0; j < nz -1 - ne; j++) {
                    c -= b[j][nr-i] * corr[l+j];
                }
                corr[l-i] = c;
            }
            l -= nrpb[k];
        }

        return corr;
    }




    public static final int diag(int nr, int nc, double[][] s, int block) {
        // block here just for error reporting;

        double[] rn = new double[nr];
        double[] pivr = new double[nc];

        // record scalings for implicit pivoting
        for (int ir = 0; ir < nr; ir++) {
            rn[ir] = 0.;
            for (int ic = 0; ic < nc-1; ic++) {
                rn[ir] = max(abs(s[ic][ir]), rn[ir]);
            }
            if (rn[ir] <= 0.0) {
                Sp("row sum 0 in block " + block + "  row " + ir);
                return -1;
            }
            rn[ir] = 1. / rn[ir];
        }

        // diagonalise left hand end
        for (int ir = 0; ir < nr; ir++) {
            // choose pivot
            int k = ir;
            double mx = abs(s[ir][ir] * rn[ir]);
            for (int l = ir+1; l < nr; l++) {
                double v = abs(rn[l] * s[ir][l]);
                if (v > mx) {
                    mx = v;
                    k = l;
                }
            }
            if (s[ir][k] == 0) {
                Sp("no pivot in block " + block + " for row " + ir);
                return -1;
            }

            double f = 1.0 / s[ir][k];
            for (int i = 0; i < nc; i++) {
                pivr[i] = s[i][k] * f;
                s[i][k] = s[i][ir];
            }
            rn[k] = rn[ir];  //----------? overwrites?

            // eliminate elements below pivot
            for (int i = ir+1; i < nr; i++) {
                double g = s[ir][i];
                for (int j = ir; j < nc; j++) {
                    s[j][i] -= g * pivr[j];
                }
            }

            // slot in pivot row
            for (int j = ir+1; j < nc; j++) {
                s[j][ir] = pivr[j];
            }
        }

        // eliminate elements above diagonal
        for (int ir = nr-2; ir >= 0; ir--) {
            for (int ic = ir+1; ic < nr; ic++) {
                double g = s[ic][ir];
                for (int j = nr; j < nc; j++) {
                    s[j][ir] -= g * s[j][ic];
                }
            }
        }

        return 0;
    }








}


