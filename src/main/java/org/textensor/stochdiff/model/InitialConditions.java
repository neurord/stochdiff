package org.textensor.stochdiff.model;

import java.util.ArrayList;
import org.textensor.report.E;
import org.textensor.stochdiff.inter.AddableTo;

import java.util.HashMap;

public class InitialConditions implements AddableTo {



    public ArrayList<ConcentrationSet> concentrationSets;
    public HashMap<String, ConcentrationSet> concSetHM;

    public ArrayList<SurfaceDensitySet> sdSets;
    public HashMap<String, SurfaceDensitySet> sdSetHM;


    ConcentrationSet defaultConcs;


    public void add(Object obj) {

        if (concentrationSets == null) {
            concentrationSets = new ArrayList<ConcentrationSet>();
            concSetHM = new HashMap<String, ConcentrationSet>();
        }

        if (sdSets == null) {
            sdSets = new ArrayList<SurfaceDensitySet>();
            sdSetHM = new HashMap<String, SurfaceDensitySet>();
        }

        if (obj instanceof ConcentrationSet) {
            ConcentrationSet cset = (ConcentrationSet)obj;
            concentrationSets.add(cset);
            if (cset.hasRegion()) {
                concSetHM.put(cset.getRegion(), cset);
            }
            if (defaultConcs == null) {
                defaultConcs = cset;
            } else {
                if (defaultConcs.hasRegion() && !cset.hasRegion()) {
                    defaultConcs = cset;
                }
            }

        } else if (obj instanceof SurfaceDensitySet) {
            SurfaceDensitySet sdset = (SurfaceDensitySet)obj;
            sdSets.add(sdset);
            if (sdset.hasRegion()) {
                sdSetHM.put(sdset.getRegion(), sdset);
            }

        } else {
            E.error("cant add " + obj);
        }
    }


    public double[] getDefaultNanoMolarConcentrations(String[] spl) {
        double[] ret = null;
        if (defaultConcs != null) {
            ret = defaultConcs.getNanoMolarConcentrations(spl);
        } else {
            ret = new double[spl.length];
        }
        return ret;
    }

    public boolean hasConcentrationsFor(String rnm) {
        return (rnm.equals("defalut") || concSetHM.containsKey(rnm));
    }


    public double[] getRegionConcentrations(String rnm, String[] spl) {
        double[] ret = null;
        if (concSetHM.containsKey(rnm)) {
            ret = concSetHM.get(rnm).getNanoMolarConcentrations(spl);
        } else if (rnm.equals("deflault")) {
            ret = defaultConcs.getNanoMolarConcentrations(spl);
        } else {
            E.error("want concentrations for unknown region " + rnm);
        }
        return ret;
    }


    public boolean hasSurfaceDensitiesFor(String rnm) {
        return (sdSetHM.containsKey(rnm));
    }

    public double[] getRegionSurfaceDensities(String rnm, String[] spl) {
        double[] ret = null;
        if (sdSetHM.containsKey(rnm)) {
            ret = sdSetHM.get(rnm).getPicoSurfaceDensities(spl);

        } else {
            E.error("want concentrations for unknown region " + rnm);
        }
        return ret;
    }

}
