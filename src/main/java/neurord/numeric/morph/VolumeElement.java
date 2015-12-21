//6 18 2007: WK added a boolean variable (submembrane), and three functions (isSubmembrane, getSubmembrane, and setSubmembrane)
//written by Robert Cannon
package neurord.numeric.morph;

import java.util.ArrayList;

import neurord.geom.Position;

public abstract class VolumeElement {
    protected final Position center;

    protected final String label;
    protected final String region;
    protected final String groupID;

    protected final double volume;
    protected final double deltaZ;
    protected final double exposedArea;

    protected int icache;

    protected final double alongArea;
    protected final double sideArea;
    protected final double topArea;

    protected final ArrayList<ElementConnection> connections = new ArrayList<>();

    protected final Position[] boundary;
    protected final Position[] surfaceBoundary;

    protected boolean fixcon = false;

    public VolumeElement(String label, String region, String groupID,
                         Position[] boundary,
                         Position[] surfaceBoundary,
                         double exposedArea,
                         Position center,
                         double alongArea, double sideArea, double topArea,
                         double volume, double deltaZ) {
        this.label = label;
        this.region = region;
        this.groupID = groupID;

        assert !(surfaceBoundary == null && exposedArea != 0.0);

        this.boundary = boundary;
        this.surfaceBoundary = surfaceBoundary;
        this.exposedArea = exposedArea;

        this.center = center;

        this.alongArea = alongArea;
        this.sideArea = sideArea;
        this.topArea = topArea;

        this.volume = volume;
        this.deltaZ = deltaZ;
    }

    public double getAlongArea() {
        return alongArea;
    }

    public double getSideArea() {
        return sideArea;
    }

    public double getTopArea() {
        return topArea;
    }

    public double getX() {
        return this.center.getX();
    }

    public double getY() {
        return this.center.getY();
    }

    public double getZ() {
        return this.center.getZ();
    }

    public boolean isSubmembrane() {
        return this.surfaceBoundary != null;
    }

    public String getLabel() {
        return label;
    }

    public String getRegion() {
        return region;
    }

    public ArrayList<ElementConnection> getConnections() {
        fixcon = true;
        return connections;
    }

    public double getVolume() {
        return this.volume;
    }

    public double getDeltaZ() {
        return this.deltaZ;
    }

    public void coupleTo(VolumeElement vx, double ca) {
        // ca is the area of contact between the elements;
        assert !fixcon;
        connections.add(new ElementConnection(this, vx, ca));
    }

    public double getExposedArea() {
        return this.exposedArea;
    }

    public void cache(int ind) {
        icache = ind;
    }

    public int getCached() {
        return icache;
    }

    public Position[] getBoundary() {
        return this.boundary;
    }

    public Position[] getSurfaceBoundary() {
        return this.surfaceBoundary;
    }

    public String getAsText() {
        StringBuffer sb = new StringBuffer();
        // export boundary if have it, ow just the center point;
        if (boundary != null)
            for (Position p : boundary)
                sb.append(String.format("%s(%.5g %.5g %.5g)",
                                        sb.length() > 0 ? " " : "",
                                        p.getX(), p.getY(), p.getZ()));
        else
            sb.append(String.format("%s(%.5g %.5g %.5g)",
                                    sb.length() > 0 ? " " : "",
                                    this.getX(), this.getY(), this.getZ()));
        return sb.toString();
    }

    public String getHeadings() {
        StringBuffer sb = new StringBuffer();
        // export boundary if have it, ow just the center point;
        if (boundary != null)
            for (int i = 0; i < boundary.length; i++)
                sb.append(" x" + i + " y" + i + " z" + i);
        else
            sb.append(" cx cy cz");

        sb.append(" volume deltaZ");
        return sb.toString();
    }

    public String getAsPlainText() {
        StringBuffer sb = new StringBuffer();
        for(double p: this.getAsNumbers())
            sb.append(String.format("%s%.5g", sb.length() > 0 ? " " : "", p));
        return sb.toString();
    }

    private double[] getAsNumbers() {
        // export boundary if have it, or just the center point;
        if (boundary != null) {
            double ans[] = new double[3 * boundary.length + 2];
            int i = 0;
            for (Position p: this.getBoundary()) {
                ans[i++] = p.getX();
                ans[i++] = p.getY();
                ans[i++] = p.getZ();
            }
            ans[i++] = volume;
            ans[i++] = deltaZ;
            assert i == ans.length;
            return ans;
        } else
            return new double[]{this.getX(), this.getY(), this.getZ(), volume, deltaZ};
    }

    public String getGroupID() {
        return this.groupID;
    }
}
