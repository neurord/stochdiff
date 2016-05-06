package neurord.numeric.morph;

public class SpinePopulation {
    private final String id;
    private final SpineProfile profile;
    private final String targetRegion;
    private final double density;

    public SpinePopulation(String id, SpineProfile profile, String targetRegion, double density) {
        this.id = id;
        this.profile = profile;
        this.targetRegion = targetRegion;
        this.density = density;
    }

    public SpineProfile getProfile() {
        return this.profile;
    }

    public String getTargetRegion() {
        return this.targetRegion;
    }

    public double getDensity() {
        return this.density;
    }

    public String getID() {
        return this.id;
    }
}
