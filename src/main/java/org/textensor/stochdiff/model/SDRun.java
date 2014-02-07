//5 16 2007: modified by RO
//written by Robert Cannon
package org.textensor.stochdiff.model;

import org.textensor.stochdiff.numeric.BaseCalc;
import org.textensor.stochdiff.numeric.BaseCalc.distribution_t;
import org.textensor.stochdiff.numeric.BaseCalc.algorithm_t;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import javax.xml.bind.annotation.*;

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

    // public double outputInterval=0.8;
    public double outputInterval;

    public String outputSpecies;

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
    public double leap_min_jump = 5.0;

    public String calculation;

    public String distribution;
    public String algorithm;

    private distribution_t distributionID;
    private algorithm_t algorithmID;

    public void resolve() {
        reactionScheme.resolve();
        morphology.resolve();
    }

    // just getters from here on;

    public distribution_t getDistribution() {
        return distribution_t.valueOf(this.distribution);
    }

    public algorithm_t getAlgorithm() {
        return algorithm_t.valueOf(this.algorithm);
    }


    public double getStateSaveInterval() {
        return stateSaveInterval;
    }

    public OutputScheme getOutputScheme() {
        return outputScheme;
    }

    public ReactionScheme getReactionScheme() {
        return reactionScheme;
    }

    public StimulationSet getStimulationSet() {
        if (this.stimulationSet != null)
            return this.stimulationSet;
        else
            return new StimulationSet();
    }

    public Morphology getMorphology() {
        return morphology;
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
