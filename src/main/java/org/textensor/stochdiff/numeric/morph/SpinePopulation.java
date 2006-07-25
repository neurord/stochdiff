package org.textensor.stochdiff.numeric.morph;


public class SpinePopulation {

    SpineProfile profile;
    String targetRegion;
    double density;



    public SpinePopulation(SpineProfile sp, String tgt, double d) {
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


}
