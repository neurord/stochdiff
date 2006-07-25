package org.textensor.stochdiff.model;

import java.util.ArrayList;
import org.textensor.report.E;
import org.textensor.stochdiff.inter.AddableTo;


public class InitialConditions implements AddableTo {



    public ArrayList<ConcentrationSet> concentrationSets;

    ConcentrationSet defaultConcs;


    public void add(Object obj) {

        if (concentrationSets == null) {
            concentrationSets = new ArrayList<ConcentrationSet>();
        }
        if (obj instanceof ConcentrationSet) {
            ConcentrationSet cset = (ConcentrationSet)obj;
            concentrationSets.add(cset);
            if (defaultConcs == null) {
                defaultConcs = cset;
            } else {
                if (defaultConcs.hasRegion() && !cset.hasRegion()) {
                    defaultConcs = cset;

                }

            }
        } else {
            E.error("cant add " + obj);
        }
    }




    public double[] getDefaultNanoMolarConcentrations(String[] spl) {
        return defaultConcs.getNanoMolarConcentrations(spl);
    }



}
