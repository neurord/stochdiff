package org.textensor.stochdiff.model;

import java.util.ArrayList;
import java.util.Arrays;
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

    @XmlElement(name="SurfaceDensitySet")
    public ArrayList<SurfaceDensitySet> sdSets;

    transient private HashMap<String, SurfaceDensitySet> sdSetHM;

    public FitConstraints fitConstraints;

    transient private ConcentrationSet defaultConcs;

    public synchronized HashMap<String, ConcentrationSet> getConcentrationSets() {
        if (this.concSetHM == null)
            this.concSetHM = listToRegionMap(this.concentrationSets);
        return this.concSetHM;
    }

    public synchronized ConcentrationSet getDefaultConcentrations() {
        if (this.defaultConcs == null) {
            if (this.concentrationSets != null)
                for (ConcentrationSet set: this.concentrationSets)
                    if (!set.hasRegion()) {
                        this.defaultConcs = set;
                        break;
                    }
            if (this.defaultConcs == null)
                this.defaultConcs = new ConcentrationSet();
        }
        return this.defaultConcs;
    }

    public double[][] makeRegionConcentrations(String[] sra, String[] species) {
        double[] baseConcentrations = this.getDefaultConcentrations().getNanoMolarConcentrations(species);
        double[][] ret = new double[sra.length][];
        for (int i = 0; i < sra.length; i++) {
            /* Use base concentrations everywhere, and just
             * override with those values which are explicitly set.
             */
            ret[i] = Arrays.copyOf(baseConcentrations, baseConcentrations.length);

            if (this.hasConcentrationsFor(sra[i])) {
                double[] wk = this.getRegionConcentrations(sra[i], species);
                for (int j = 0; j < baseConcentrations.length; j++)
                    // FIXME: use nans
                    if (wk[j] >= 0.)
                        ret[i][j] = wk[j];
            }
        }
        return ret;
    }

    public double[][] makeRegionSurfaceDensities(String[] sra, String[] species) {
        double[][] ret = new double[sra.length][];
        for (int i = 0; i < sra.length; i++)
            if (this.hasSurfaceDensitiesFor(sra[i]))
                ret[i] = this.getRegionSurfaceDensities(sra[i], species);

        return ret;
    }


    public synchronized HashMap<String, SurfaceDensitySet> getSurfaceDensitySets() {
        if (this.sdSetHM == null)
            this.sdSetHM = listToRegionMap(this.sdSets);
        return this.sdSetHM;
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

    public boolean hasConcentrationsFor(String rnm) {
        return rnm.equals("default") || this.getConcentrationSets().containsKey(rnm);
    }


    public double[] getRegionConcentrations(String rnm, String[] spl) {
        if (this.getConcentrationSets().containsKey(rnm))
            return this.getConcentrationSets().get(rnm).getNanoMolarConcentrations(spl);
        else if (rnm.equals("default"))
            return this.getDefaultConcentrations().getNanoMolarConcentrations(spl);
        else {
            log.error("want concentrations for unknown region '{}'", rnm);
            throw new RuntimeException("want concentrations for unknown region " + rnm);
        }
    }


    public boolean hasSurfaceDensitiesFor(String rnm) {
        return this.getSurfaceDensitySets().containsKey(rnm);
    }

    public double[] getRegionSurfaceDensities(String rnm, String[] spl) {
        if (this.getSurfaceDensitySets().containsKey(rnm))
            return this.getSurfaceDensitySets().get(rnm).getPicoSurfaceDensities(spl);
        else {
            log.error("want surface densities for unknown region '{}'", rnm);
            throw new RuntimeException("want surface densities for unknown region " + rnm);
        }
    }


    public String[] getTotalPreserved() {
        if (fitConstraints != null)
            return fitConstraints.getTotalPreserved();
        else
            return new String[0];
    }

    public static <T extends Regional> HashMap<String, T> listToRegionMap(List<T> list) {
        HashMap<String, T> hm = inst.newHashMap();

        if (list != null)
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
