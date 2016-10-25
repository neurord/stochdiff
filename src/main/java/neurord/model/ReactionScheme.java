package neurord.model;


import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.bind.annotation.*;

import neurord.numeric.chem.ReactionTable;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class ReactionScheme {
    static final Logger log = LogManager.getLogger();

    @XmlElement(name="Specie")
    private final ArrayList<Specie> species = new ArrayList<>();

    @XmlElement(name="Reaction")
    private final ArrayList<Reaction> reactions = new ArrayList<>();

    static <T,V> void hmPut(HashMap<T, V> hm, T key, V value) {
        V old = hm.put(key, value);
        if (old != null)
            throw new RuntimeException("overwriting key " + key + " with new value");
    }

    public void resolve() {
        HashMap<String, Specie> hm = new HashMap<>();
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
            /* We create a forward reaction if forwardRate > 0 (this is the usual case).
             * We create a reverse reaction if the reverseRate is nonzero.
             */
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
            boolean have_forward = r.getForwardRate() > 0;

            if (have_forward)
                rtab.setReactionData(i++, reactants, products, r.getForwardRate(), false);

            if (r.getReverseRate() > 0)
                /* We need to guard against reactions which have forwardRate==0 */
                rtab.setReactionData(i++, products, reactants, r.getReverseRate(), have_forward);
        }

        assert i == n: "ir=" + i + " n=" + n;

        return rtab;
    }
}
