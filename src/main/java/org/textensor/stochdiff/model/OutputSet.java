package org.textensor.stochdiff.model;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

import org.textensor.util.inst;
import org.textensor.util.ArrayUtil;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import javax.xml.bind.annotation.*;

public class OutputSet implements IOutputSet {
    static final Logger log = LogManager.getLogger(OutputSet.class);

    @XmlAttribute public String filename;
    @XmlAttribute public String region;
    @XmlAttribute public Double outputInterval;

    @XmlElement(name="OutputSpecie")
    public ArrayList<OutputSpecie> outputSpecies;

    @Override
    public List<String> getNamesOfOutputSpecies() {
        if (this.outputSpecies == null)
            return null;

        List<String> names = inst.newArrayList();
        for (OutputSpecie out: this.outputSpecies)
            names.add(out.name);
        return names;
    }

    public static int[] outputSpecieIndices(String where, List<String> specout, String[] species) {
        if (specout == null)
            return ArrayUtil.iota(species.length);

        HashMap<String, Integer> map = inst.newHashMap();
        for (int i = 0; i < species.length; i++) {
            if (species[i].equals("all"))
                return ArrayUtil.iota(species.length);
            map.put(species[i], i);
        }

        int[] ans = new int[specout.size()];
        int i = 0;
        for (String so: specout) {
            Integer k = map.get(so);
            if (k == null) {
                log.error("Unknown output species '{}' " +
                          "(requested for output in {} but not found in ReactionScheme)", so, where);
                throw new RuntimeException("Unknown species '" + so + "'");
            }
            ans[i++] = k;
        }

        return ans;
    }

    @Override
    public int[] getIndicesOfOutputSpecies(String[] species) {
        List<String> output = this.getNamesOfOutputSpecies();
        String where = "OutputSet " + (filename != null ? filename : "w/o filename");
        return outputSpecieIndices(where, output, species);
    }

    @Override
    public String getRegion() {
        if (this.region != null)
            return this.region;
        else
            return "default";
    }

    @Override
    public String getIdentifier() {
        return this.filename;
    }

    @Override
    public double getOutputInterval(double fallback) {
        if (this.outputInterval != null)
            return this.outputInterval;
        else
            return fallback;
    }
}
