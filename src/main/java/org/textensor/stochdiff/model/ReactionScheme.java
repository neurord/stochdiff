package org.textensor.stochdiff.model;


import java.util.ArrayList;
import java.util.HashMap;

import org.textensor.report.E;
import org.textensor.stochdiff.inter.AddableTo;
import org.textensor.stochdiff.numeric.chem.ReactionTable;


public class ReactionScheme implements AddableTo {

    public ArrayList<Specie> species;
    HashMap<String, Specie> specieHM;

    public ArrayList<Reaction> reactions;
    HashMap<String, Reaction> reactionHM;

    private Specie[] specieArray;  // fixed in the order used for calculations;

    public ReactionScheme() {
        species = new ArrayList<Specie>();
        reactions = new ArrayList<Reaction>();
    }


    public void add(Object obj) {
        if (obj instanceof Specie) {
            species.add((Specie)obj);
        } else if (obj instanceof Reaction) {
            reactions.add((Reaction)obj);
        } else {
            E.error("cant add " + obj + " to reaction scheme ");
        }
    }


    public void resolve() {
        for (Reaction r : reactions) {
            r.resolve(getSpecieHM());
        }
    }


    public ArrayList<Specie> getSpecieList() {
        return species;
    }

    public ArrayList<Reaction> getReactionList() {
        return reactions;
    }


    public HashMap<String, Specie> getSpecieHM() {
        if (specieHM == null) {
            specieHM = new HashMap<String, Specie>();
            for (Specie sp : species) {
                specieHM.put(sp.getID(), sp);
            }
        }
        return specieHM;
    }


    public String[] getSpeciesIDList() {
        String[] ret = new String[species.size()];
        int ict = 0;
        for (Specie sp : species) {
            ret[ict++] = sp.getID();
        }
        return ret;
    }


    public double[] getDiffusionConstants() {
        double[] ret = new double[species.size()];
        int ict = 0;
        for (Specie sp : species) {
            ret[ict++] = sp.getDiffusionConstant();
        }
        return ret;
    }


    public void indexSpecies() {
        // TODO - can optimize the order here;
        specieArray = new Specie[species.size()];
        int ict = 0;
        for (Specie sp : species) {
            specieArray[ict] = sp;
            sp.setIndex(ict);
            ict += 1;
        }
    }



    public ReactionTable makeReactionTable() {
        if (specieArray == null) {
            indexSpecies();
        }
        int nreaction = reactions.size();
        int nspecie = species.size();
        ReactionTable rtab = new ReactionTable(2 * nreaction, nspecie);


        int ir = 0;
        for (Reaction r :  reactions) {
            r.writeForwardToTable(rtab, ir);
            ir++;
            r.writeReverseToTable(rtab, ir);
            ir++;
        }

        rtab.setSpeciesIDs(getSpeciesIDList());
        rtab.setDiffusionConstants(getDiffusionConstants());

        return rtab;
    }




}
