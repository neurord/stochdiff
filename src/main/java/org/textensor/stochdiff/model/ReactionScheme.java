package org.textensor.stochdiff.model;


import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.bind.annotation.*;

import org.textensor.stochdiff.numeric.chem.ReactionTable;
import org.textensor.util.inst;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class ReactionScheme {
    static final Logger log = LogManager.getLogger();

    @XmlElement(name="Specie")
    private final ArrayList<Specie> species = inst.newArrayList();

    @XmlElement(name="Reaction")
    private final ArrayList<Reaction> reactions = inst.newArrayList();

    static <T,V> void hmPut(HashMap<T, V> hm, T key, V value) {
        V old = hm.put(key, value);
        if (old != null)
            throw new RuntimeException("overwriting key " + key + " with new value");
    }

    public void resolve() {
        HashMap<String, Specie> hm = inst.newHashMap();
        int index = 0;

        for (Specie sp: this.species) {
            sp.setIndex(index++);
            hmPut(hm, sp.getID(), sp);
            if (!sp.getName().equals(sp.getID()))
                hmPut(hm, sp.getName(), sp);
        }

        for (Reaction r : reactions)
            r.resolve(hm);
    }

    protected String[] getSpecies() {
        String[] ret = new String[species.size()];
        int ict = 0;
        for (Specie sp : species)
            ret[ict++] = sp.getID();

        return ret;
    }

    protected double[] getDiffusionConstants() {
        double[] ret = new double[species.size()];
        int ict = 0;
        for (Specie sp : species)
            ret[ict++] = sp.getDiffusionConstant();

        return ret;
    }

    public ReactionTable makeReactionTable() {
        int n  = 0;
        for (Reaction r: reactions) {
            /* There's always a forward reaction, but if a
             * reaction has no products, there's no reverse reaction.
             * If a reaction has 0 rate, we can skip that too. */
            if (r.getForwardRate() > 0)
                n++;
            if (r.getReverseRate() > 0)
                n++;
        }

        log.info("Running with {} reactions (forward and reverse)", n);

        ReactionTable rtab = new ReactionTable(n, this.getSpecies(), this.getDiffusionConstants());

        int i = 0;
        for (Reaction r: reactions) {
            int[][] reactants = r.getReactantIndices();
            int[][] products = r.getProductIndices();

            if (r.getForwardRate() > 0)
                rtab.setReactionData(i++, reactants, products, r.getForwardRate(), false);

            if (r.getReverseRate() > 0)
                rtab.setReactionData(i++, products, reactants, r.getReverseRate(), true);
        }

        assert i == n: "ir=" + i + " n=" + n;

        return rtab;
    }
}
