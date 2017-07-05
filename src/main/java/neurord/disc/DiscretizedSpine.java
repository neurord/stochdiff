package neurord.disc;

public class DiscretizedSpine {

    private final double[] boundaries;
    private final double[] widths;
    private final String[] labels;
    private final String[] regions;

    public DiscretizedSpine(double[] boundaries, double[] widths, String[] labels, String[] regions) {

        this.boundaries = boundaries;
        this.widths = widths;
        this.labels = labels;
        this.regions = regions;
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
