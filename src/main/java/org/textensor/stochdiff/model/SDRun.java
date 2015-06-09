//5 16 2007: modified by RO
//written by Robert Cannon
package org.textensor.stochdiff.model;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.List;

import org.textensor.stochdiff.numeric.BaseCalc;
import org.textensor.stochdiff.numeric.BaseCalc.distribution_t;
import org.textensor.stochdiff.numeric.BaseCalc.algorithm_t;
import org.textensor.stochdiff.numeric.chem.ReactionTable;
import org.textensor.util.ArrayUtil;
import org.textensor.util.inst;
import org.textensor.xml.StringListAdapter;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.*;

@XmlRootElement(name="SDRun")
public class SDRun {
    static final Logger log = LogManager.getLogger(SDRun.class);

    @XmlElement(name="ReactionScheme")
    private ReactionScheme reactionScheme;

    @XmlElement(name="StimulationSet")
    private StimulationSet stimulationSet;

    @XmlElement(name="Morphology")
    private Morphology morphology;

    @XmlElement(name="InitialConditions")
    private InitialConditions initialConditions;

    @XmlElement(name="OutputScheme")
    private OutputScheme outputScheme;

    private Discretization discretization;

    public String reactionSchemeFile;
    public String morphologyFile;
    public String stimulationFile;
    public String initialConditionsFile;
    public String outputSchemeFile;

    public String initialStateFile;
    public double stateSaveInterval;
    public String stateSavePrefix;


    public String action;

    public String geometry = "2D";
    public double depth2D = 0.5;

    public double runtime;
    public double starttime;
    public double endtime;

    public String output;


    public int spineSeed;
    public int simulationSeed;

    // time step for fixed step calculations;
    public double fixedStepDt = Float.POSITIVE_INFINITY;

    private double outputInterval;

    @XmlJavaTypeAdapter(StringListAdapter.class)
    private List<String> outputSpecies;
    private transient int[] _outputSpecies;

    public String outputQuantity = "NUMBER"; // either "NUMBER" or "CONCENTRATION"

    /**
     * Accepted tolerance for adaptive calculations
     * (delta f / f  for an algorithm dependent function f).
     */
    public double tolerance = 0.001;

    /**
     * How many times our calculated allowed leap must be longer than
     * normal event waiting time, for us to choose leaping.
     */
    public double leap_min_jump = 1.0;

    public String calculation;

    public String distribution;
    public String algorithm;

    private distribution_t distributionID;
    private algorithm_t algorithmID;

    // just getters from here on;

    public distribution_t getDistribution() {
        return distribution_t.valueOf(this.distribution);
    }

    public algorithm_t getAlgorithm() {
        return algorithm_t.valueOf(this.algorithm);
    }

    public double getOutputInterval() {
        return this.outputInterval;
    }

    public double getStateSaveInterval() {
        return stateSaveInterval;
    }

    public OutputScheme getOutputScheme() {
        return outputScheme;
    }

    transient private boolean _reactionSchemeResolved = false;
    public ReactionScheme getReactionScheme() {
        if (!this._reactionSchemeResolved) {
            if (this.reactionScheme != null)
                this.reactionScheme.resolve();
            this._reactionSchemeResolved =true;
        }
        return this.reactionScheme;
    }

    transient private ReactionTable reactionTable;
    public ReactionTable getReactionTable() {
        if (this.reactionTable == null)
            this.reactionTable = this.getReactionScheme().makeReactionTable();
        return this.reactionTable;
    }

    public StimulationSet getStimulationSet() {
        if (this.stimulationSet != null)
            return this.stimulationSet;
        else
            return new StimulationSet();
    }

    transient private boolean _morphologyResolved = false;
    public Morphology getMorphology() {
        if (!this._morphologyResolved) {
            if (this.morphology != null)
                this.morphology.resolve();
            this._morphologyResolved =true;
        }
        return this.morphology;
    }

    private static int[] outputSpecieIndices(List<String> specout, String[] species) {
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
                          "(requested or output but not in reaction scheme)", so);
                throw new RuntimeException("unknown species '" + so + "'");
            }
            ans[i++] = k;
        }

        return ans;
    }

    public int[] getOutputSpecies() {
        if (this._outputSpecies == null) {
            ReactionScheme rs = this.getReactionScheme();
            this._outputSpecies = outputSpecieIndices(this.outputSpecies,
                                                      rs.getSpecies());
        }
        return this._outputSpecies;
    }

    public Discretization getDiscretization() {
        return discretization;
    }

    public InitialConditions getInitialConditions() {
        return initialConditions;
    }

    public double getStartTime() {
        return starttime;
    }

    public double getEndTime() {
        if (endtime > 0)
            return endtime;
        else if (runtime > 0)
            return runtime - getStartTime();
        else {
            log.error("Either runtime or endtime must be specified in the model file");
            throw new RuntimeException("Either runtime or endtime must be specified in the model file");
        }
    }

    public boolean continueOutput() {
        boolean ret = false;
        if (output == null) {
            // fine - not specified
        } else {
            String lco = output.toLowerCase().trim();
            if (lco.equals("continue")) {
                ret = true;
            } else if (lco.equals("new")) {
                ret = false;
            } else {
                log.error("Unrecognized output option: " + output + " (expecting 'new' or 'continue')");
                throw new RuntimeException("Unrecognized output option: " + output + " (expecting 'new' or 'continue')");
            }
        }
        return ret;
    }
}
