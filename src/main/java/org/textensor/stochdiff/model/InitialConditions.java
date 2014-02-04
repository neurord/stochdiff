package org.textensor.stochdiff.model;

import java.util.ArrayList;
import java.util.HashMap;
import javax.xml.bind.annotation.*;

import org.textensor.report.E;
import org.textensor.stochdiff.inter.AddableTo;
import org.textensor.stochdiff.inter.FloatValued;

public class InitialConditions implements AddableTo {


    @XmlElement(name="ConcentrationSet")
    public ArrayList<ConcentrationSet> concentrationSets;

    transient public HashMap<String, ConcentrationSet> concSetHM;

    @XmlElement(name="SurfanceDensitySet")
    public ArrayList<SurfaceDensitySet> sdSets;

    transient public HashMap<String, SurfaceDensitySet> sdSetHM;


    public FitConstraints fitConstraints;

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

        } else if (obj instanceof FitConstraints) {
            fitConstraints = (FitConstraints)obj;

        } else {
            E.error("cannot add " + obj);
        }
    }


    public ArrayList<FloatValued> getFloatValuedElements() {
        ArrayList<FloatValued> afv = new ArrayList<FloatValued>();
        if (concentrationSets != null) {
            for (ConcentrationSet cs : concentrationSets) {
                cs.addFloatValued(afv);
            }
        }

        if (sdSets != null) {
            for (SurfaceDensitySet sdSet : sdSets) {
                sdSet.addFloatValued(afv);
            }
        }
        return afv;
    }


    public double[] getDefaultNanoMolarConcentrations(String[] spl) {
        double[] ret = null;
        if (defaultConcs != null) {
            ret = defaultConcs.getNanoMolarConcentrations(spl);
        } else {
            ret = new double[spl.length];
        }
        // set to zero where previously undefined (indicated by negative return from getNanoMolarConcentrations)
        for (int i = 0; i < ret.length; i++) {
            if (ret[i] < 0.) {
                ret[i] = 0.;
            }
        }
        return ret;
    }

    public boolean hasConcentrationsFor(String rnm) {
        return (rnm.equals("default") || concSetHM.containsKey(rnm));
    }


    public double[] getRegionConcentrations(String rnm, String[] spl) {
        double[] ret = null;
        if (concSetHM.containsKey(rnm)) {
            ret = concSetHM.get(rnm).getNanoMolarConcentrations(spl);
        } else if (rnm.equals("default")) {
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


    public String xmlSerialize() {
        StringBuffer sb = new StringBuffer();
        sb.append("<InitialConditions>\n");


        for (ConcentrationSet cset : concentrationSets) {
            sb.append("<ConcentrationSet");
            if (cset.hasRegion()) {
                sb.append("region=\"" + cset.getRegion() + "\">\n");
            } else {
                sb.append(">\n");
            }
            for (Concentration c : cset.concentrations) {
                sb.append("   " + c.makeXMLLine() + "\n");
            }
            sb.append("</ConcentrationSet>\n");
        }

        for (SurfaceDensitySet sdset : sdSets) {
            sb.append("<SurfaceDensitySet");
            if (sdset.hasRegion()) {
                sb.append("region=\"" + sdset.getRegion() + "\">\n");
            } else {
                sb.append(">\n");
            }
            for (SurfaceDensity sd : sdset.sds) {
                sb.append("   " + sd.makeXMLLine() + "\n");
            }
            sb.append("</SurfaceDensitySet>\n");
        }



        sb.append("</InitialConditions>\n");
        return sb.toString();
    }


    public String[] getTotalPreserved() {
        String[] ret = new String[0];
        if (fitConstraints != null) {
            ret = fitConstraints.getTotalPreserved();
        }
        return ret;
    }


}
