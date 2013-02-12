package org.textensor.stochdiff.model;

import java.util.ArrayList;
import java.util.HashMap;

import org.textensor.report.E;
import org.textensor.stochdiff.inter.AddableTo;
import org.textensor.stochdiff.inter.FloatValued;

public class ConcentrationSet implements AddableTo {

    public String region;

    public ArrayList<Concentration> concentrations;

    HashMap<String, Concentration> concHM;

    public boolean complete;



    public void add(Object obj) {
        if (concentrations == null) {
            concentrations = new ArrayList<Concentration>();
        }
        if (obj instanceof Concentration) {
            concentrations.add((Concentration)obj);
        } else {
            E.error("cannot add " + obj);
        }
    }



    public HashMap<String, Concentration> getConcHM() {
        if (concHM == null) {
            concHM = new HashMap<String, Concentration>();
            if (concentrations != null) {
                for (Concentration c : concentrations) {
                    concHM.put(c.specieID, c);
                }
            }
        }
        return concHM;
    }


    public double[] getNanoMolarConcentrations(String[] ids) {
        double[] ret = new double[ids.length];
        HashMap<String, Concentration> chm = getConcHM();

        for (int i = 0; i < ids.length; i++) {
            if (chm.containsKey(ids[i])) {
                ret[i] = chm.get(ids[i]).getNanoMolarConcentration();
            } else {
                ret[i] = -1.;
            }
        }

        return ret;
    }


    public boolean hasRegion() {
        return (region != null);
    }

    public String getRegion() {
        return region;
    }



    public void addFloatValued(ArrayList<FloatValued> afv) {
        afv.addAll(concentrations);

    }

}
