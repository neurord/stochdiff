package org.textensor.stochdiff.numeric.morph;


public class SpinePopulation {

    String id;
    SpineProfile profile;
    String targetRegion;
    double density;



    public SpinePopulation(String sid, SpineProfile sp, String tgt, double d) {
        id = sid;
        profile = sp;
        targetRegion = tgt;
        density = d;
    }


    public SpineProfile getProfile() {
        return profile;
    }

    public String getTargetRegion() {
        return targetRegion;
    }

    public double getDensity() {
        return density;
    }

    public String getID() {
        return id;
    }

}
