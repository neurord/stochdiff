package org.textensor.stochdiff.model;

import java.util.ArrayList;
import java.util.HashMap;

import java.util.StringTokenizer;

import org.textensor.report.E;
import org.textensor.stochdiff.inter.AddableTo;
import org.textensor.stochdiff.numeric.chem.ReactionTable;
import org.textensor.util.inst;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class Reaction implements AddableTo {
    static final Logger log = LogManager.getLogger(Reaction.class);

    public String name;
    public String id;

    private final ArrayList<Reactant> p_reactants = inst.newArrayList();
    private final ArrayList<Product> p_products = inst.newArrayList();

    public double forwardRate;
    public double reverseRate;

    public double Q10;

    private ArrayList<Specie> r_reactants;
    private ArrayList<Specie> r_products;

    public void add(Object obj) {
        if (obj instanceof Reactant)
            this.p_reactants.add((Reactant)obj);
        else if (obj instanceof Product)
            this.p_products.add((Product)obj);
        else
            throw new RuntimeException("cannot add " + obj);
    }

    public void resolve(HashMap<String, Specie> sphm) {
        if (this.p_reactants.isEmpty() || this.p_products.isEmpty())
            log.warn("no reactants in reaction {}", name);
        this.r_reactants = parseRefs(this.p_reactants, sphm);
        this.r_products = parseRefs(this.p_products, sphm);
    }

    private ArrayList<Specie> parseRefs(ArrayList<? extends SpecieRef> asr,
                                        HashMap<String, Specie> sphm) {

        ArrayList<Specie> ret = inst.newArrayList();
        for (SpecieRef sr : asr) {
            Specie sr2 = sphm.get(sr.getSpecieID());
            if (sr2 == null)
                throw new RuntimeException
                    ("reaction " + name + " mentions unknown specie " + sr);
            ret.add(sr2);
        }
        return ret;
    }

    /**
     * Returns an array [2 x nspecies] containing species and
     * their counts (n) in reaction. This is the "fake" multiplicity,
     * which does not influence propensity, only the number of molecules
     * destroyed or produced in the reaction.
     */
    private static int[][] getIndices(ArrayList<Specie> spa,
                                      ArrayList<? extends SpecieRef> refs) {
        int n = spa.size();
        int[][] ret = new int[2][n];

        for (int i = 0; i < n; i++)
            ret[0][i] = spa.get(i).getIndex();

        for (int i = 0; i < n; i++)
            ret[1][i] = refs.get(i).getN();

        return ret;
    }

    /**
     * Returns an array [2 x nspecies] containing reactants and
     * their counts (n) in reaction. This is the "fake" multiplicity,
     * which does not influence propensity, only the number of molecules
     * destroyed or produced in the reaction.
     */
    public int[][] getReactantIndices() {
        return getIndices(this.r_reactants, this.p_reactants);
    }

    /**
     * Returns an array [2 x nspecies] containing products and
     * their counts (n) in reaction. This is the "fake" multiplicity,
     * which does not influence propensity, only the number of molecules
     * destroyed or produced in the reaction.
     */
    public int[][] getProductIndices() {
        return getIndices(this.r_products, this.p_products);
    }
}
