package neurord.numeric.morph;


public class SpineProfile {

    String id;

    double[] lpoints;
    double[] widths;

    String[] labels;
    String[] regions;

    public SpineProfile(String sid, double[] vl, double[] vw,
                        String[] lbls, String[] rgs) {
        id = sid;
        lpoints = vl;
        widths = vw;

        labels = lbls;
        regions = rgs;
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

    public String[] getLabels() {
        return labels;
    }

    public String[] getRegions() {
        return regions;
    }
}
