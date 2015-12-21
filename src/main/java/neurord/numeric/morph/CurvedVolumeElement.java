package neurord.numeric.morph;

import neurord.geom.Position;

public class CurvedVolumeElement extends VolumeElement {

    int iradial;
    int iazimuthal;

    int[] stripLengths;
    float[][] verts;
    float[][] norms;

    public CurvedVolumeElement(String label, String region, String groupID,
                               Position[] boundary,
                               double volume, double deltaZ) {

        super(label, region, groupID,
              boundary,
              0.0, 0.0, 0.0,
              volume, deltaZ);
    }

    public void setPositionIndexes(int ir, int ia) {
        iradial = ir;
        iazimuthal = ia;
    }

    public void setTriangles(int[] sls, float[][] vs, float[][] ns) {
        stripLengths = sls;
        verts = vs;
        norms = ns;
    }

    public String getText3D() {
        StringBuffer sb = new StringBuffer();
        sb.append("" + stripLengths.length + " " + verts.length + "\n");
        int nsl = stripLengths.length;
        for (int i = 0; i < nsl; i++) {
            sb.append("" + stripLengths[i] + " ");
        }
        sb.append("\n");
        int nv = verts.length;
        for (int i = 0; i < verts.length; i++) {
            float fv[] = verts[i];
            float fn[] = norms[i];
            sb.append(String.format("%.3f %.3f %.3f %.2f %.2f %.2f\n", fv[0], fv[1], fv[2], fn[0], fn[1], fn[2]));
        }
        return sb.toString();
    }
}
