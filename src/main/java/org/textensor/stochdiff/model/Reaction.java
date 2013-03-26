package org.textensor.stochdiff.model;

import java.util.ArrayList;
import java.util.HashMap;

import java.util.StringTokenizer;

import org.textensor.report.E;
import org.textensor.stochdiff.inter.AddableTo;
import org.textensor.stochdiff.numeric.chem.ReactionTable;
import org.textensor.util.inst;

public class Reaction implements AddableTo {

    public String name;
    public String id;

    private ArrayList<Reactant> p_reactants = inst.newArrayList();
    private ArrayList<Product> p_products = inst.newArrayList();

    public double forwardRate;
    public double reverseRate;

    public double michaelisConstant;

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
            throw new RuntimeException("no reactants in reaction " + name);
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

    private int[][] getIndices(ArrayList<Specie> spa,
                               ArrayList<? extends SpecieRef> refs) {
        int n = spa.size();
        int[][] ret = new int[2][n];

        for (int i = 0; i < n; i++)
            ret[0][i] = spa.get(i).getIndex();

        for (int i = 0; i < n; i++)
            ret[1][i] = refs.get(i).getN();

        return ret;
    }


    public void writeForwardToTable(ReactionTable rtab, int ir) {
        rtab.setReactionData(ir, getIndices(r_reactants, p_reactants),
                             getIndices(r_products, p_products), forwardRate);
    }


    public void writeReverseToTable(ReactionTable rtab, int ir) {
        rtab.setReactionData(ir, getIndices(r_products, p_products),
                             getIndices(r_reactants, p_reactants), reverseRate);

    }


}
