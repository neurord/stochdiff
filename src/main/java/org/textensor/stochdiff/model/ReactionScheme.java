package org.textensor.stochdiff.model;


import java.util.ArrayList;
import java.util.HashMap;

import org.textensor.report.E;
import org.textensor.stochdiff.inter.AddableTo;
import org.textensor.stochdiff.numeric.chem.ReactionTable;
import org.textensor.util.inst;


public class ReactionScheme implements AddableTo {

    private final ArrayList<Specie> species = inst.newArrayList();
    private final HashMap<String, Specie> specieHM = inst.newHashMap();

    private final ArrayList<Reaction> reactions = inst.newArrayList();

    static <T,V> void hmPut(HashMap<T, V> hm, T key, V value) {
        V old = hm.put(key, value);
        if (old != null)
            throw new RuntimeException("overwriting key " + key + " with new value");
    }

    public void add(Object obj) {
        if (obj instanceof Specie) {
            Specie sp = (Specie) obj;
            sp.setIndex(this.species.size());
            this.species.add(sp);
            hmPut(this.specieHM, sp.getID(), sp);
        } else if (obj instanceof Reaction) {
            reactions.add((Reaction)obj);
        } else {
            throw new RuntimeException("cannot add " + obj + " to reaction scheme ");
        }
    }

    public void resolve() {
        for (Reaction r : reactions)
            r.resolve(this.specieHM);
    }

    protected String[] getSpecieIDs() {
        String[] ret = new String[species.size()];
        int ict = 0;
        for (Specie sp : species) {
            ret[ict++] = sp.getID();
        }
        return ret;
    }

    protected double[] getDiffusionConstants() {
        double[] ret = new double[species.size()];
        int ict = 0;
        for (Specie sp : species) {
            ret[ict++] = sp.getDiffusionConstant();
        }
        return ret;
    }

    public ReactionTable makeReactionTable() {
        int nreaction = reactions.size();
        int nspecie = species.size();
        ReactionTable rtab = new ReactionTable(2 * nreaction, nspecie);

        int ir = 0;
        for (Reaction r :  reactions) {
            int[][] reactants = r.getReactantIndices();
            int[][] products = r.getProductIndices();
            rtab.setReactionData(ir++, reactants, products, r.forwardRate);
            rtab.setReactionData(ir++, products, reactants, r.reverseRate);
        }

        rtab.setSpeciesIDs(getSpecieIDs());
        rtab.setDiffusionConstants(getDiffusionConstants());

        return rtab;
    }
}
