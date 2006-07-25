package org.textensor.stochdiff.numeric.morph;


public class SpineProfile {

    String id;

    double[] lpoints;
    double[] widths;

    String[] labels;

    public SpineProfile(String sid, double[] vl, double[] vw,
                        String[] lbls) {
        id = sid;
        lpoints = vl;
        widths = vw;

        labels = lbls;
    }

    public String getID() {
        return id;
    }


    public double[] getXPts() {
        return lpoints;
    }


    public double[] getWidths() {
        return widths;
    }


}
