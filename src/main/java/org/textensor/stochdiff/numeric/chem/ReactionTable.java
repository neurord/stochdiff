package org.textensor.stochdiff.numeric.chem;

import org.textensor.report.E;
import org.textensor.stochdiff.numeric.math.Matrix;
import org.textensor.stochdiff.numeric.math.Column;


public class ReactionTable {

    int nreaction;
    public int nspecie;
    String[] speciesIDs;
    double[] diffusionConstants;

    int[][] reactantIndices;
    int[][] productIndices;

    int[][] reactantStochiometry;
    int[][] productStochiometry;

    double[] rates;


    Matrix productionMatrix;


    public ReactionTable(int nr, int ns) {
        nreaction = nr;
        nspecie = ns;

        reactantIndices = new int[nreaction][2];
        productIndices = new int[nreaction][2];

        reactantStochiometry = new int[nreaction][2];
        productStochiometry = new int[nreaction][2];

        for (int i = 0; i < nreaction; i++) {
            for (int j = 0; j < 2; j++) {
                reactantIndices[i][j] = -1;
                productIndices[i][j] = -1;
            }
        }

        rates = new double[nreaction];

    }


    public String[] getSpecieIDs() {
        return speciesIDs;
    }

    public void print() {
        StringBuffer sb = new StringBuffer();
        sb.append("nspecie = " + nspecie + "  nreaction = " + nreaction + "\n");
        for (int i = 0; i < nreaction; i++) {
            sb.append("reaction " + i + ":      ");
            sb.append("" + reactantIndices[i][0]);
            sb.append(" (" + reactantStochiometry[i][0] + ")");
            if (reactantIndices[i][1] >= 0) {
                sb.append(" + " + reactantIndices[i][1]);
                sb.append(" (" + reactantStochiometry[i][1] + ")");
            }

            sb.append(" --> ");
            sb.append("" + productIndices[i][0]);
            sb.append(" (" + productStochiometry[i][0] + ")");
            if (productIndices[i][1] >= 0) {
                sb.append(" + " + productIndices[i][1]);
                sb.append(" (" + productStochiometry[i][1] + ")");
            }
            sb.append("   rate " + rates[i] + " \n");
        }
        E.info(sb.toString());
    }



    public void setReactionData(int ireact, int[][] aidx, int[][] bidx, double rate) {
        // restrict to max 2 reactants, 2 products - faster than general case;


        reactantIndices[ireact][0] = aidx[0][0];
        reactantStochiometry[ireact][0] = aidx[1][0];
        if (aidx[0].length > 1) {
            reactantIndices[ireact][1] = aidx[0][1];
            reactantStochiometry[ireact][1] = aidx[1][1];
        }

        productIndices[ireact][0] = bidx[0][0];
        productStochiometry[ireact][0] = bidx[1][0];
        if (bidx[0].length > 1) {
            productIndices[ireact][1] = bidx[0][1];
            productStochiometry[ireact][1] = bidx[1][1];
        }
        rates[ireact] = rate;


        if (aidx.length > 2) {
            E.error("cannot handle reactions with more than two reactants");
        }
        if (bidx.length > 2) {
            E.error("cannot handle reactions with more than two products");
        }



    }


    public void setCatalyzedReactionData(int ireact, int na, int nb, int icat, int[][] aidx,
                                         int[][] bidx, double rate) {
        /*
         * nreactant[ireact] = na; nproduct[ireact] = nb; for (int i = 0; i < na;
         * i++) { reactantIndices[ireact][i] = aidx[i]; } for (int i = 0; i < nb;
         * i++) { productIndices[ireact][i] = bidx[i]; } rates[ireact] = rate;
         */
        E.missing();
    }



    public void setSpeciesIDs(String[] sa) {
        speciesIDs = sa;
    }


    public String[] getSpeciesIDs() {
        return speciesIDs;
    }


    public int getNSpecies() {
        return nspecie;
    }


    public void setDiffusionConstants(double[] d) {
        diffusionConstants = d;
    }


    public double[] getDiffusionConstants() {
        return diffusionConstants;
    }


    public Column getRateColumn(Column mconc) {
        double[] c = mconc.getData();
        double[] vr = new double[nreaction];
        for (int ireac = 0; ireac < nreaction; ireac++) {
            int[] si = reactantIndices[ireac];

            double r = rates[ireac];
            r *= c[si[0]];
            if (si[1] >= 0) {
                r *= c[si[1]];
            }
            vr[ireac] = r;
        }
        return new Column(vr);
    }


    public Matrix getProductionMatrix() {
        if (productionMatrix == null) {
            double[][] a = new double[nspecie][nreaction];
            for (int ireac = 0; ireac < nreaction; ireac++) {
                int[] si = reactantIndices[ireac];
                a[si[0]][ireac] -= 1;
                if (si[1] >= 0) {
                    a[si[1]][ireac] -= 1;
                }


                int[] pi = productIndices[ireac];
                a[pi[0]][ireac] += 1;
                if (pi[1] >= 0) {
                    a[pi[1]][ireac] += 1;
                }
            }
            productionMatrix = new Matrix(a);
        }
        return productionMatrix;
    }


    // this is the same as the production matrix times teh rate column;
    public Column getProductionColumn(Column mconc) {
        double[] c = mconc.getData();
        double[] vr = new double[nspecie];

        for (int ireac = 0; ireac < nreaction; ireac++) {
            int[] si = reactantIndices[ireac];
            int[] pi = productIndices[ireac];

            double r = rates[ireac];
            r *= c[si[0]];
            if (si[1] >= 0) {
                r *= c[si[1]];
            }

            vr[si[0]] -= r;
            if (si[1] >= 0) {
                vr[si[1]] -= r;
            }

            vr[pi[0]] += r;
            if (pi[1] >= 0) {
                vr[pi[1]] += r;
            }
        }
        return new Column(vr);
    }


    /*
     * This is the matrix M such that delta C = productionColumn + M delta C
     */

    public Matrix getIncrementRateMatrix(Column mconc) {
        double[][] d = new double[nspecie][nspecie];

        double[] c = mconc.getData();

        for (int ireac = 0; ireac < nreaction; ireac++) {
            int[] si = reactantIndices[ireac];
            int[] pi = productIndices[ireac];

            {
                double r = rates[ireac];
                if (si[1] >= 0) {
                    r *= c[si[1]];
                }
                int isrc = si[0];

                d[si[0]][isrc] -= r;
                if (si[1] >= 0) {
                    d[si[1]][isrc] -= r;
                }

                // TODO - identify A+A reactions etc and multiply by combinatorial
                // factor!!!!!!!!;
                d[pi[0]][isrc] += r;
                if (pi[1] >= 0) {
                    d[pi[1]][isrc] += r;
                }
            }

            if (si[1] >= 0) {
                double r = rates[ireac];
                r *= c[si[0]];
                int isrc = si[1];

                d[si[0]][isrc] -= r;
                if (si[1] >= 0) {
                    d[si[1]][isrc] -= r;
                }

                // TODO - identify A+A reactions etc and multiply by combinatorial
                // factor!!!!!!!!;
                d[pi[0]][isrc] += r;
                if (pi[1] >= 0) {
                    d[pi[1]][isrc] += r;
                }


            }
        }
        return new Matrix(d);
    }



    public final Column stepResiduals(Column vc, Column vdc, double dt) {

        Column vctot = vc.plus(vdc);
        double[] ctot = vctot.getData();

        Column vret = vdc.copy();
        double[] ret = vdc.getData();

        for (int ireac = 0; ireac < nreaction; ireac++) {
            int[] srcIdx = reactantIndices[ireac];
            int[] prodIdx = productIndices[ireac];

            double r = rates[ireac];
            r *= ctot[srcIdx[0]];
            if (srcIdx[1] >= 0) {
                r *= ctot[srcIdx[1]];
            }
            ret[srcIdx[0]] += r;
            if (srcIdx[1] >= 0) {
                ret[srcIdx[1]] += r;
            }

            ret[prodIdx[0]] -= r;
            if (prodIdx[1] >= 0) {
                ret[prodIdx[1]] += r;
            }

        }
        return vret;
    }


    public int getNReaction() {
        return nreaction;
    }


    public double[] getRates() {
        return rates;
    }



    public int[][] getReactantIndices() {
        return reactantIndices;
    }


    public int[][] getProductIndices() {
        return productIndices;
    }


    public int[][] getReactantStochiometry() {
        return reactantStochiometry;
    }


    public int[][] getProductStochiometry() {
        return productStochiometry;
    }


    public int getSpecieIndex(String specieID) {
        String[] sa = getSpeciesIDs();

        for (int i = 0; i < nspecie; i++)
            if (sa[i].equals(specieID))
                return i;
        E.dump("specs", sa);
        throw new RuntimeException("cannot find specie " + specieID +
                                   " required for stimulation");
    }


    // could also be useful to hava analytic derivatives of the above residuals



}
