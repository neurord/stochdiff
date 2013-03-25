package org.textensor.stochdiff.model;

import java.util.ArrayList;
import java.util.HashMap;

import java.util.StringTokenizer;

import org.textensor.report.E;
import org.textensor.stochdiff.inter.AddableTo;
import org.textensor.stochdiff.numeric.chem.ReactionTable;


public class Reaction implements AddableTo {

    public String name;
    public String id;

    public String reactants;
    public String products;

    private ArrayList<Reactant> p_reactants;
    private ArrayList<Product> p_products;

    public double forwardRate;
    public double reverseRate;

    public double michaelisConstant;

    public double Q10;

    private ArrayList<Specie> r_reactants;
    private ArrayList<Specie> r_products;

    public void add(Object obj) {
        if (obj instanceof Reactant) {
            if (p_reactants == null) {
                p_reactants = new ArrayList<Reactant>();
            }
            p_reactants.add((Reactant)obj);
        } else if (obj instanceof Product) {
            if (p_products == null) {
                p_products = new ArrayList<Product>();
            }
            p_products.add((Product)obj);

        } else {
            E.error("cannot add " + obj);
        }

    }


    public void resolve(HashMap<String, Specie> sphm) {
        if (reactants != null) {
            r_reactants = parseList(reactants, sphm);

        } else if (p_reactants != null) {
            r_reactants = parseRefs(p_reactants, sphm);
        } else {
            E.error("no reactants? ");
        }

        if (products != null) {
            r_products = parseList(reactants, sphm);
        } else if (p_products != null) {
            r_products = parseRefs(p_products, sphm);
        } else {
            E.error("no reactants? ");
        }
    }


    private ArrayList<Specie> parseList(String lst, HashMap<String, Specie> sphm) {

        ArrayList<Specie> ret = new ArrayList<Specie>();
        StringTokenizer st = new StringTokenizer(lst, " ,");
        while (st.hasMoreTokens()) {
            String tok = st.nextToken();
            if (sphm.containsKey(tok)) {
                ret.add(sphm.get(tok));
            } else {
                E.error("reaction " + name + " mentions unknown specie " + tok);
            }

        }
        return ret;
    }



    private ArrayList<Specie> parseRefs(ArrayList<? extends SpecieRef> asr,
                                        HashMap<String, Specie> sphm) {

        ArrayList<Specie> ret = new ArrayList<Specie>();
        for (SpecieRef sr : asr) {

            if (sphm.containsKey(sr.getSpecieID())) {
                ret.add(sphm.get(sr.getSpecieID()));
            } else {
                E.error("reaction " + name + " mentions unknown specie " + sr);
            }

        }
        return ret;
    }




    private int[][] getIndices(ArrayList<Specie> spa, ArrayList<? extends SpecieRef> refs) {
        int n = spa.size();
        int[][] ret = new int[2][n];
        for (int i = 0; i < n; i++) {
            ret[0][i] = spa.get(i).getIndex();
            ret[1][i] = 1;
        }
        if (refs != null) {
            for (int i = 0; i < n; i++) {
                ret[1][i] = refs.get(i).getN();
            }
        }
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
