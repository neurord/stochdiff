package org.textensor.stochdiff.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.HashMap;

import javax.xml.bind.annotation.*;

import org.textensor.stochdiff.inter.FloatValued;
import org.textensor.util.inst;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class InitialConditions {
    static final Logger log = LogManager.getLogger(InitialConditions.class);

    @XmlElement(name="ConcentrationSet")
    public ArrayList<ConcentrationSet> concentrationSets;

    transient private HashMap<String, ConcentrationSet> concSetHM;

    @XmlElement(name="SurfanceDensitySet")
    public ArrayList<SurfaceDensitySet> sdSets;

    transient private HashMap<String, SurfaceDensitySet> sdSetHM;

    public FitConstraints fitConstraints;

    ConcentrationSet defaultConcs;

    public synchronized HashMap<String, ConcentrationSet> getConcentrationSets() {
        if (concSetHM == null)
            concSetHM = listToRegionMap(this.concentrationSets);
        return concSetHM;
    }

    public synchronized HashMap<String, SurfaceDensitySet> getSurfaceDensitySets() {
        if (sdSetHM == null)
            sdSetHM = listToRegionMap(this.sdSets);
        return sdSetHM;
    }

    public ArrayList<FloatValued> getFloatValuedElements() {
        ArrayList<FloatValued> afv = new ArrayList<FloatValued>();

        if (concentrationSets != null)
            for (ConcentrationSet cs : concentrationSets)
                cs.addFloatValued(afv);

        if (sdSets != null)
            for (SurfaceDensitySet sdSet : sdSets)
                sdSet.addFloatValued(afv);

        return afv;
    }


    public double[] getDefaultNanoMolarConcentrations(String[] spl) {
        if (defaultConcs != null) {
            double[] ret = defaultConcs.getNanoMolarConcentrations(spl);

            // set to zero where previously undefined (indicated by
            // negative return from getNanoMolarConcentrations)
            for (int i = 0; i < ret.length; i++)
                if (ret[i] < 0)
                    ret[i] = 0;

            return ret;
        } else
            return new double[spl.length];
    }

    public boolean hasConcentrationsFor(String rnm) {
        return (rnm.equals("default") || concSetHM.containsKey(rnm));
    }


    public double[] getRegionConcentrations(String rnm, String[] spl) {
        if (concSetHM.containsKey(rnm))
            return concSetHM.get(rnm).getNanoMolarConcentrations(spl);
        else if (rnm.equals("default"))
            return defaultConcs.getNanoMolarConcentrations(spl);
        else {
            log.error("want concentrations for unknown region '{}'", rnm);
            throw new RuntimeException("want concentrations for unknown region " + rnm);
        }
    }


    public boolean hasSurfaceDensitiesFor(String rnm) {
        return (sdSetHM.containsKey(rnm));
    }

    public double[] getRegionSurfaceDensities(String rnm, String[] spl) {
        if (sdSetHM.containsKey(rnm))
            return sdSetHM.get(rnm).getPicoSurfaceDensities(spl);
        else {
            log.error("want surface densities for unknown region '{}'", rnm);
            throw new RuntimeException("want surface densities for unknown region " + rnm);
        }
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
        if (fitConstraints != null)
            return fitConstraints.getTotalPreserved();
        else
            return new String[0];
    }

    public static <T extends Regional> HashMap<String, T> listToRegionMap(List<T> list) {
        HashMap<String, T> hm = inst.newHashMap();

        for(T item: list)
            if (item.hasRegion()) {
                T old = hm.put(item.getRegion(), item);
                if (old != null) {
                    log.error("Duplicate {} for region '{}'",
                              item.getClass().getSimpleName(), item.getRegion());
                    throw new RuntimeException("Duplicate " + item.getClass());
                }
            }

        return hm;
    }
}
