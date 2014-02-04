package org.textensor.stochdiff.model;


import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.bind.annotation.*;

import org.textensor.stochdiff.inter.AddableTo;
import org.textensor.stochdiff.numeric.chem.ReactionTable;
import org.textensor.util.inst;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class ReactionScheme implements AddableTo {
    static final Logger log = LogManager.getLogger(ReactionScheme.class);

    @XmlElement(name="Specie")
    private final ArrayList<Specie> species = inst.newArrayList();

    private final transient HashMap<String, Specie> specieHM = inst.newHashMap();

    @XmlElement(name="Reaction")
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
            if (!sp.getName().equals(sp.getID()))
                hmPut(this.specieHM, sp.getName(), sp);
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
        int n  = 0;
        for (Reaction r: reactions)
            /* There's always a forward reaction, but if a
             * reaction has no products, there's no reverse reaction.
             * If a reaction has 0 rate, we can skip that too. */
            n += (r.getForwardRate() > 0 ? 1 : 0) +
                (r.getProductIndices()[0].length > 0 &&
                 r.getReverseRate() > 0 ? 1 : 0);

        log.info("Running with {} reactions (forward and reverse)", n);

        ReactionTable rtab = new ReactionTable(n, species.size());

        rtab.setSpeciesIDs(getSpecieIDs());
        rtab.setDiffusionConstants(getDiffusionConstants());

        int i = 0;
        for (Reaction r :  reactions) {
            int[][] reactants = r.getReactantIndices();
            int[][] products = r.getProductIndices();

            if (r.getForwardRate() > 0)
                rtab.setReactionData(i++, reactants, products, r.getForwardRate());

            if (r.getReverseRate() > 0)
                if (products[1].length > 0)
                    rtab.setReactionData(i++, products, reactants, r.getReverseRate());
                else
                    throw new RuntimeException("reaction with non-zero rate but no reactants: "
                                               + r.getID());
        }

        assert i == n: "ir=" + i + " n=" + n;

        return rtab;
    }
}
