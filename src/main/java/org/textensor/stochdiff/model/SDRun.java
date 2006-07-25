package org.textensor.stochdiff.model;


public class SDRun {

    public String reactionSchemeFile;
    public String morphologyFile;
    public String stimulationFile;
    public String initialConditionsFile;


    public String geometry;

    public double runtime;

    public int spineSeed;
    public double spineDeltaX;


    // discretization should ensure no boxes end up larger than this;
    public double maxElementSide;


    //  volume to use for a single mixed pool calculation : could (should?) be computed
    // by summing the volume in the supplied morphology
    public double poolVolume;

    // time step for fixed step calculations;
    public double fixedStepDt;

    // accepted tolerance for adaptive calculations (delta f / f  for an algorithm dependent function f);
    public double tolerance;


    public String calculation;



    public ReactionScheme reactionScheme;
    public StimulationSet stimulationSet;
    public Morphology morphology;
    public InitialConditions initialConditions;



    public void resolve() {
        reactionScheme.resolve();
        morphology.resolve();
    }




    // just getters and setters from here on;

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



    public void setInitialConditions(InitialConditions initialConditions) {
        this.initialConditions = initialConditions;
    }



    public InitialConditions getInitialConditions() {
        return initialConditions;
    }





}
