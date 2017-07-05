package neurord.numeric.morph;


public class SpineProfile {

    private final String id;

    private final double[] lpoints;
    private final double[] widths;

    private final String[] labels;
    private final String[] regions;

    public SpineProfile(String id, double[] lpoints, double[] widths,
                        String[] labels, String[] regions) {
        this.id = id;
        this.lpoints = lpoints;
        this.widths = widths;

        this.labels = labels;
        this.regions = regions;
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
