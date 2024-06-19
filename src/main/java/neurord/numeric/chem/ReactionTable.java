package neurord.numeric.chem;

import static java.lang.String.format;
import java.util.Arrays;

import neurord.numeric.math.Matrix;
import neurord.numeric.math.Column;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class ReactionTable {
    static final Logger log = LogManager.getLogger();

    public final int nreaction;

    private final String[] species;
    private final double[] diffusionConstants;

    private final int[][] reactantIndices;
    private final int[][] productIndices;

    private final int[][] reactantStoichiometry;
    private final int[][] productStoichiometry;

    private final int[][] reactantPowers;

    private final int[] reversiblePairs;

    private final double[] rates;

    private Matrix productionMatrix;


    public ReactionTable(int nreaction, String[] species, double[] diffusionConstants) {
        this.nreaction = nreaction;

        this.species = species;

        this.diffusionConstants = diffusionConstants;

        this.reactantIndices = new int[nreaction][];
        this.productIndices = new int[nreaction][];

        this.reactantStoichiometry = new int[nreaction][];
        this.productStoichiometry = new int[nreaction][];

        this.reactantPowers = new int[nreaction][];

        this.reversiblePairs = new int[nreaction];

        this.rates = new double[nreaction];
    }
    

    public void print() {
        StringBuffer sb = new StringBuffer();
        sb.append("nspecie = " + this.species.length + "  nreaction = " + nreaction + "\n");
        for (int r = 0; r < nreaction; r++) {
            sb.append("reaction " + r + ":      ");
            for (int i = 0; i < reactantIndices[r].length; i++)
                sb.append(format("%s%d (%d/%d)",
                                 i == 0 ? "" : ", ",
                                 reactantIndices[r][i],
                                 reactantStoichiometry[r][i],
                                 reactantPowers[r][i]));
            sb.append(" --> ");
            for (int i = 0; i < productIndices[r].length; i++)
                sb.append(format("%s%d (%d)",
                                 i == 0 ? "" : ", ",
                                 productIndices[r][i],
                                 productStoichiometry[r][i]));
            sb.append("   rate " + rates[r] + " \n");
        }
        log.info(sb);
    }

    static Integer findDuplicates(int[] indices) {
        int[] c = Arrays.copyOf(indices, indices.length);
        Arrays.sort(c);
        for (int i = 0; i < indices.length-1; i++)
            if (c[i] == c[i+1])
                return c[i];
        return null;
    }

    public void setReactionData(int ireact, int[][] aidx, int[][] bidx, double rate, boolean is_reverse) {
        log.debug("ireact={}/{} {}→{} rate={}",
                  ireact, nreaction, aidx, bidx, rate);

        assert this.species != null;

        Integer dupl = findDuplicates(aidx[0]);
        if (dupl != null) {
            log.error("Duplicate reactant {} in reaction {}: {}", species[dupl], ireact,
                      getReactionSignature(aidx[0], aidx[1], bidx[0], bidx[1], this.species));
            throw new RuntimeException("Duplicate reactant in reaction " + ireact);
        }

        dupl = findDuplicates(bidx[0]);
        if (dupl != null) {
            log.error("Duplicate product {} in reaction {}: {}", species[dupl], ireact,
                      getReactionSignature(aidx[0], aidx[1], bidx[0], bidx[1], this.species));
            throw new RuntimeException("Duplicate product in reaction " + ireact);
        }

        reactantIndices[ireact] = aidx[0];
        reactantStoichiometry[ireact] = aidx[1];
        reactantPowers[ireact] = aidx[2];

        productIndices[ireact] = bidx[0];
        productStoichiometry[ireact] = bidx[1];

        rates[ireact] = rate;

        if (is_reverse)
            this.reversiblePairs[ireact] = ireact - 1;
        else
            this.reversiblePairs[ireact] = -1;
    }

    public String[] getSpecies() {
        assert this.species != null;
        return this.species;
    }

    public static String getReactionSignature(int[] rr, int[] rs, int[] pp, int[] ps, String[] ids) {
        StringBuffer b = new StringBuffer();

        if (rr.length > 0)
            for (int i = 0; i < rr.length; i++) {
                if (i > 0)
                    b.append("+");
                if (rs[i] > 1)
                    b.append("" + rs[i] + "×");
                b.append(ids[rr[i]]);
            }
        else
            b.append("nil");
        b.append("→");
        if (pp.length > 0)
            for (int i = 0; i < pp.length; i++) {
                if (i > 0)
                    b.append("+");
                if (ps[i] > 1)
                    b.append("" + ps[i] + "×");
                b.append(ids[pp[i]]);
            }
        else
            b.append("nil");
        return b.toString();
    }

    public int getNSpecies() {
        return this.species.length;
    }

    public double[] getDiffusionConstants() {
        return this.diffusionConstants;
    }

    public Column getRateColumn(Column mconc) {
        double[] c = mconc.getData();
        double[] vr = new double[nreaction];
        for (int ireac = 0; ireac < nreaction; ireac++) {
            double r = rates[ireac];
            for (int index: reactantIndices[ireac])
                r *= c[index];

            vr[ireac] = r;
        }
        return new Column(vr);
    }


    public Matrix getProductionMatrix() {
        if (productionMatrix == null) {
            double[][] a = new double[this.species.length][nreaction];
            for (int ireac = 0; ireac < nreaction; ireac++) {
            	int[] si = reactantIndices[ireac];
                int[] pi = productIndices[ireac];
            	int[] rstoich = reactantStoichiometry[ireac];
            	int[] pstoich = productStoichiometry[ireac];
            	
            	for (int j = 0; j < si.length; j++)
            		a[si[j]][ireac] -= rstoich[j];

            	for (int j = 0; j < pi.length; j++)
            		a[pi[j]][ireac] += pstoich[j];

             // TODO: A.S.: ADD SUPPORT FOR HIGHER REACTION ORDERS!!!
            }
            productionMatrix = new Matrix(a);
        }
        return productionMatrix;
    }


    // this is the same as the production matrix times teh rate column;
    public Column getProductionColumn(Column mconc) {
        double[] c = mconc.getData();
        double[] vr = new double[this.species.length];

        for (int ireac = 0; ireac < nreaction; ireac++) {
            int[] si = reactantIndices[ireac];
            int[] pi = productIndices[ireac];
        	int[] rstoich = reactantStoichiometry[ireac];
        	int[] pstoich = productStoichiometry[ireac];

            double r = rates[ireac];
            for (int index: reactantIndices[ireac])
                r *= c[index];
            
            for (int j = 0; j < si.length; j++)
            	vr[si[j]] -= r * rstoich[j];
            
            for (int j = 0; j < pi.length; j++)
            	vr[pi[j]] += r * pstoich[j];

            // TODO: A.S.: ADD SUPPORT FOR HIGHER REACTION ORDERS!!!
        }
        return new Column(vr);
    }


    /*
     * This is the matrix M such that ΔC = productionColumn + M ΔC
     */

    public Matrix getIncrementRateMatrix(Column mconc) {
        double[] c = mconc.getData();
        double[][] d = new double[this.species.length][this.species.length];

        for (int ireac = 0; ireac < nreaction; ireac++) {
                // Guards against cases with a [reactant] == 0
                boolean reactantsPresent = true;
                // Check if any reactant concentration is zero
                // or holds a negative residue from a previous call.
            for (int index : reactantIndices[ireac]) {
                if (c[index] <= 0) {
                    reactantsPresent = false;
                    break;
                }
            }

            if (reactantsPresent) {
                for (int index: reactantIndices[ireac]) {
                        double r = rates[ireac];

                        for (int index2: reactantIndices[ireac])
                                if (index2 != index)
                                        r *= c[index2];

                        for (int index2: reactantIndices[ireac])
                                d[index2][index] -= r;

                        // TODO - identify A+A reactions etc and multiply by combinatorial
                        // factor!!!!!!!!;
                        for (int index2: productIndices[ireac])
                                d[index2][index] += r;
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
            double r = rates[ireac];
            for (int index: reactantIndices[ireac])
                r *= ctot[index];

            for (int index: reactantIndices[ireac])
                ret[index] += r;

            for (int index: productIndices[ireac])
                ret[index] -= r;
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

    /**
     * The number of particles destroyed in the reaction (by specie).
     */
    public int[][] getReactantStoichiometry() {
        return reactantStoichiometry;
    }

    /**
     * The number of particles created in the reaction (by specie).
     */
    public int[][] getProductStoichiometry() {
        return productStoichiometry;
    }

    /**
     * Propensity powers for true second- and higher-order reactions.
     */
    public int[][] getReactantPowers() {
        return reactantPowers;
    }

    /**
     * Returns an array of indicies of the "other" reaction. Each
     * pair only gets one entry. If a reaction is not reversible, -1
     * is set.
     */
    public int[] getReversiblePairs() {
        return this.reversiblePairs;
    }

    public int getSpecieIndex(String specieID) {
        String[] species = this.getSpecies();

        for (int i = 0; i < this.species.length; i++)
            if (species[i].equals(specieID))
                return i;
        log.error("Cannot find specie {}", specieID);
        throw new RuntimeException("Cannot find specie " + specieID);
    }

    // could also be useful to hava analytic derivatives of the above residuals
}
