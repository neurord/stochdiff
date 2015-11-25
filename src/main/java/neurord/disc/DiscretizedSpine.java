package neurord.disc;

public class DiscretizedSpine {

    double[] boundaries;
    double[] widths;
    String[] labels;
    String[] regions;

    public DiscretizedSpine(double[] xbd, double[] wv, String[] lbls, String[] rgns) {
        boundaries = xbd;
        widths = wv;
        labels = lbls;
        regions = rgns;
    }


    public double[] getBoundaries() {
        return boundaries;
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
