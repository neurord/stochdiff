//5 16 2007: modified by RO (modifications within <--RO ... RO-->)
//written by Robert Cannon
package org.textensor.stochdiff.model;

import org.textensor.report.E;
import org.textensor.stochdiff.numeric.BaseCalc;
import org.textensor.stochdiff.numeric.BaseCalc.distribution_t;

public class SDRun {

    public String reactionSchemeFile;
    public String morphologyFile;
    public String stimulationFile;
    public String initialConditionsFile;
    //<--RO
    // ------------------------
    public String outputSchemeFile;
    // ------------------------
    //RO-->


    public String initialStateFile;
    public double stateSaveInterval;
    public String stateSavePrefix;


    public String action;

    public String geometry;
    public double depth2D;

    public double runtime;
    public double starttime;
    public double endtime;

    public String output;


    public int spineSeed;
    public int simulationSeed;


    //  volume to use for a single mixed pool calculation : could (should?) be computed
    // by summing the volume in the supplied morphology
    public double poolVolume;

    // time step for fixed step calculations;
    public double fixedStepDt;

    // public double outputInterval=0.8;
    public double outputInterval;

    public String outputSpecies;

    public String outputQuantity; // either "NUMBER" or "CONCENTRATION"

    // accepted tolerance for adaptive calculations (delta f / f  for an algorithm dependent function f);
    public double tolerance;


    public String calculation;

    public String distribution;
    public String algorithm;

    private distribution_t distributionID;
    private int algorithmID;


    public ReactionScheme reactionScheme;
    public StimulationSet stimulationSet;
    public Morphology morphology;
    public InitialConditions initialConditions;
    //<--RO
    // ------------------------
    public OutputScheme outputScheme;
    // ------------------------
    //RO-->

    public Discretization discretization;


    public void resolve() {
        resolveCalcTypes();
        reactionScheme.resolve();
        morphology.resolve();
    }


    private void resolveCalcTypes() {

        if (distribution != null && distribution.length() > 0) {
            if (distribution.toLowerCase().equals("binomial")) {
                distributionID = distribution_t.BINOMIAL;
            } else if (distribution.toLowerCase().equals("poisson")) {
                distributionID = distribution_t.POISSON;
            } else {
                E.shortWarning("Unrecognized distribution (" + distribution
                               + ") expecting binomial or poisson");
            }
        }

        if (algorithm != null && algorithm.length() > 0) {
            if (algorithm.toLowerCase().equals("independent")) {
                algorithmID = BaseCalc.INDEPENDENT;
            } else if (algorithm.toLowerCase().equals("shared")) {
                algorithmID = BaseCalc.SHARED;
            } else if (algorithm.toLowerCase().equals("particle")) {
                algorithmID = BaseCalc.PARTICLE;

            } else {
                E.shortWarning("Unrecognized algorithm (" + algorithm
                               + ") expecting independent or shared");
            }
        }
    }

    // just getters and setters from here on;

    public distribution_t getDistributionID() {
        return distributionID;
    }

    public int getAlgorithmID() {
        return algorithmID;
    }


    public double getStateSaveInterval() {
        return stateSaveInterval;
    }



    //<--RO
    // -------------------------
    public void setOutputScheme(OutputScheme outputScheme) {
        this.outputScheme = outputScheme;
    }

    public OutputScheme getOutputScheme() {
        return outputScheme;
    }
    // -------------------------
    //RO-->

    public void setReactionScheme(ReactionScheme reactionScheme) {
        this.reactionScheme = reactionScheme;
    }

    public ReactionScheme getReactionScheme() {
        return reactionScheme;
    }

    public void setStimulationSet(StimulationSet stimulationSet) {
        this.stimulationSet = stimulationSet;
    }

    public StimulationSet getStimulationSet() {
        return stimulationSet;
    }

    public void setMorphology(Morphology morphology) {
        this.morphology = morphology;
    }

    public Morphology getMorphology() {
        return morphology;
    }

    public Discretization getDiscretization() {
        return discretization;
    }


    public void setInitialConditions(InitialConditions initialConditions) {
        this.initialConditions = initialConditions;
    }



    public InitialConditions getInitialConditions() {
        return initialConditions;
    }


    public double getStartTime() {
        double ret = 0;
        if (starttime > 0) {
            ret = starttime;
        } else {
            // leave it at 0;
        }
        return ret;
    }


    public double getEndTime() {
        double ret = 0;
        if (endtime > 0) {
            ret = endtime;
        } else if (runtime > 0) {
            ret = runtime - getStartTime();
        } else {
            E.error("Either runtime or endtime must be specified in the model file");
        }
        return ret;
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
                E.warning("Unrecognized output option: " + output + " (expecting 'new' or 'continue')");
            }
        }
        return ret;
    }






}
