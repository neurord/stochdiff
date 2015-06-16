//5 16 2007: modified by RO
//written by Robert Cannon
package org.textensor.stochdiff.model;

import java.util.List;

import org.textensor.stochdiff.numeric.BaseCalc;
import org.textensor.stochdiff.numeric.BaseCalc.distribution_t;
import org.textensor.stochdiff.numeric.BaseCalc.algorithm_t;
import org.textensor.stochdiff.numeric.morph.VolumeGrid.geometry_t;
import org.textensor.stochdiff.numeric.chem.ReactionTable;
import org.textensor.stochdiff.numeric.chem.StimulationTable;
import org.textensor.util.ArrayUtil;
import org.textensor.util.inst;
import org.textensor.xml.StringListAdapter;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.*;

@XmlRootElement(name="SDRun")
public class SDRun implements IOutputSet {
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
    public OutputScheme outputScheme;

    private Discretization discretization;

    public String initialStateFile;
    public double stateSaveInterval;
    public String stateSavePrefix;

    public String action;

    private String geometry = "2D";
    public double depth2D = 0.5;

    private Double runtime;
    private Double starttime;
    private Double endtime;

    public String output;


    public int spineSeed;
    public int simulationSeed;

    // time step for fixed step calculations;
    private Double fixedStepDt;

    private double outputInterval;

    @XmlJavaTypeAdapter(StringListAdapter.class)
    private List<String> outputSpecies;

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

    public geometry_t getGeometry() {
        return geometry_t.fromString(this.geometry);
    }

    @Override
    public String getRegion() {
        return null;
    }

    @Override
    public String getIdentifier() {
        return "out";
    }

    @Override
    public double getOutputInterval(double fallback) {
        return this.outputInterval;
    }

    public double getOutputInterval() {
        return this.outputInterval;
    }

    public double getStateSaveInterval() {
        return stateSaveInterval;
    }

    public double getFixedStepDt() {
        if (this.fixedStepDt != null)
            return this.fixedStepDt;
        else
            return Float.POSITIVE_INFINITY;
    }

    public List<? extends IOutputSet> getOutputSets() {
        if (this.outputScheme != null)
            return this.outputScheme.outputSets;
        else
            return null;
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

    transient private StimulationTable stimulationTable;
    public StimulationTable getStimulationTable() {
        if (this.stimulationTable == null) {
            if (this.stimulationSet != null)
                this.stimulationTable = this.stimulationSet.makeStimulationTable(this.getReactionTable());
            else
                this.stimulationTable = new StimulationTable();
            assert this.stimulationTable != null;
        }
        return this.stimulationTable;
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

    public String[] getSpecies() {
        return this.getReactionScheme().getSpecies();
    }

    @Override
    public List<String> getNamesOfOutputSpecies() {
        return this.outputSpecies;
    }

    @Override
    public int[] getIndicesOfOutputSpecies(String[] species) {
        return OutputSet.outputSpecieIndices("outputSpecies", this.outputSpecies, species);
    }

    public Discretization getDiscretization() {
        if (discretization != null)
            return discretization;
        else
            return Discretization.SINGLE_VOXEL;
    }

    public InitialConditions getInitialConditions() {
        return initialConditions;
    }

    public double getStartTime() {
        if (this.starttime != null)
            return this.starttime;
        else
            return 0;
    }

    public double getEndTime() {
        if (this.endtime != null)
            return this.endtime;
        else if (this.runtime != 0)
            return this.runtime + getStartTime();
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
