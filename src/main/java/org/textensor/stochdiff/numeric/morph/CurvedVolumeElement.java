package org.textensor.stochdiff.numeric.morph;

import org.textensor.stochdiff.geom.Position;

public class CurvedVolumeElement extends VolumeElement {


    double alongArea;
    double sideArea;
    double topArea;

    int iradial;
    int iazimuthal;


    int[] stripLengths;
    float[][] verts;
    float[][] norms;


    public void setAlongArea(double d) {
        alongArea = d;

    }

    public double getAlongArea() {
        return alongArea;
    }

    public void setSideArea(double d) {
        sideArea = d;
    }
    public double getSideArea() {
        return sideArea;
    }


    public void setTopArea(double d) {
        topArea = d;
    }

    public double getTopArea() {
        return topArea;
    }



    public String getAsText() {
        StringBuffer sb = new StringBuffer();
        // export boundary if have it, ow just the center point;
        if (boundary != null) {
            for (Position p : boundary) {
                sb.append(String.format(" (%.5g %.5g %.5g) ", p.getX(), p.getY(), p.getZ()));
            }
        } else {
            sb.append(String.format(" (%.5g %.5g %.5g) ", cx, cy, cz));

        }
        return sb.toString();
    }

    @SuppressWarnings("boxing")
    public String getAsPlainText() {
        StringBuffer sb = new StringBuffer();
        // export boundary if have it, ow just the center point;
        if (boundary != null) {
            for (Position p : boundary) {
                sb.append(String.format(" %.5g %.5g %.5g", p.getX(), p.getY(), p.getZ()));
            }
        } else {
            sb.append(String.format(" %.5g %.5g %.5g", cx, cy, cz));
        }
        sb.append(String.format(" %.5g %.5g", volume, deltaZ));
        return sb.toString();
    }


    @SuppressWarnings("boxing")
    public String getHeadings() {
        StringBuffer sb = new StringBuffer();
        // export boundary if have it, ow just the center point;
        if (boundary != null) {
            for (int i = 0; i < boundary.length; i++) {
                sb.append(" x" + i + " y" + i + " z" + i);

            }
        } else {
            sb.append(" cx cy cz");

        }
        sb.append(" volume deltaZ");
        return sb.toString();
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
