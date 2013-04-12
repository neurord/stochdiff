package org.textensor.stochdiff.numeric.chem;

import org.textensor.report.E;
import org.textensor.stochdiff.numeric.math.Matrix;
import org.textensor.stochdiff.numeric.math.Column;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import static java.lang.String.format;

public class ReactionTable {
    static final Logger log = LogManager.getLogger(ReactionTable.class);

    public final int nreaction;
    public final int nspecie;

    private String[] speciesIDs;
    private double[] diffusionConstants;

    private final int[][] reactantIndices;
    private final int[][] productIndices;

    private final int[][] reactantStochiometry;
    private final int[][] productStochiometry;

    private final double[] rates;

    private Matrix productionMatrix;


    public ReactionTable(int nreaction, int nspecie) {
        this.nreaction = nreaction;
        this.nspecie = nspecie;

        this.reactantIndices = new int[nreaction][];
        this.productIndices = new int[nreaction][];

        this.reactantStochiometry = new int[nreaction][];
        this.productStochiometry = new int[nreaction][];

        this.rates = new double[nreaction];
    }


    public void print() {
        StringBuffer sb = new StringBuffer();
        sb.append("nspecie = " + nspecie + "  nreaction = " + nreaction + "\n");
        for (int r = 0; r < nreaction; r++) {
            sb.append("reaction " + r + ":      ");
            for (int i = 0; i < reactantIndices[r].length; i++)
                sb.append(format("%s%d (%d/%d)",
                                 reactantIndices[r][i],
                                 reactantStochiometry[r][i],
                                 reactantPowers[r][i],
                                 i == 0 ? "" : ", "));
            sb.append(" --> ");
            for (int i = 0; i < productIndices[r].length; i++)
                sb.append(format("%s%d (%d)",
                                 productIndices[r][i],
                                 productStochiometry[r][i],
                                 i == 0 ? "" : ", "));
            sb.append("   rate " + rates[r] + " \n");
        }
        log.info(sb);
    }

    public void setReactionData(int ireact, int[][] aidx, int[][] bidx, double rate) {
        log.info("nreaction={} nspecie={} ireact={} aix={} bidx={} rate={}",
                 nreaction, nspecie, ireact, aidx, bidx, rate);
        reactantIndices[ireact] = aidx[0];
        reactantStochiometry[ireact] = aidx[1];

        productIndices[ireact] = bidx[0];
        productStochiometry[ireact] = bidx[1];

        rates[ireact] = rate;
    }


    public void setSpeciesIDs(String[] sa) {
        speciesIDs = sa;
    }


    public String[] getSpecieIDs() {
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
        String[] sa = getSpecieIDs();

        for (int i = 0; i < nspecie; i++)
            if (sa[i].equals(specieID))
                return i;
        E.dump("specs", sa);
        throw new RuntimeException("cannot find specie " + specieID +
                                   " required for stimulation");
    }


    // could also be useful to hava analytic derivatives of the above residuals



}
