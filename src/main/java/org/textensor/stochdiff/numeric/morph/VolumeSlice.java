package org.textensor.stochdiff.numeric.morph;

import org.textensor.report.E;
import org.textensor.stochdiff.geom.*;

import java.util.ArrayList;

public class VolumeSlice {

    int nx;
    int ny;
    double xSize;
    double ySize;

    VolumeElement[][] elements;

    public VolumeSlice(int n, double w) {
        nx = n;
        ny = n;
        xSize = w;
        ySize = w;
    }


    public VolumeElement getElement(int i, int j) {
        return elements[i][j];
    }


    public void discFill(Position pa, Position pb) {
        double cx = 0.5 * xSize;
        double cy = 0.5 * ySize;

        E.missing("need to do surface areas");

        double seglength = Geom.distanceBetween(pa, pb);

        Translation trans = Geom.translation(Geom.midpoint(pa, pb));
        Rotation rot = Geom.fromZRotation(Geom.fromToVector(pa, pb));

        elements = new VolumeElement[nx][ny];

        double r2 = cx * cx + cy * cy;

        double dx = xSize / nx;
        double dy = ySize / ny;
        for (int i = 0; i < nx; i++) {
            for (int j = 0; j < ny; j++) {
                double vcx = -cx + (i + 0.5) * dx;
                double vcy =  -cy + (j + 0.5) * dy;
                double rc2 = vcx * vcx + vcy * vcy;

                if (rc2 < r2) {
                    VolumeElement ve = new VolumeElement();

                    Position cp = Geom.position(vcx, vcy, 0.);
                    Position pr = rot.getRotatedPosition(cp);
                    Position pc = trans.getTranslated(pr);
                    ve.setCenterPosition(pc.getX(), pc.getY(), pc.getZ());
                    ve.setVolume(dx * dy * seglength);
                    elements[i][j] = ve;

                    ve.setAlongArea(dy * seglength);
                    ve.setSideArea(dx * dy);
                    ve.setTopArea(dx * seglength);

                } else {

                }
            }
        }
        neighborize();
    }



    public void neighborize() {
        for (int i = 0; i < nx; i++) {
            for (int j = 0; j < ny; j++) {
                VolumeElement v = elements[i][j];
                VolumeElement vx = null;
                VolumeElement vy = null;
                if (i+1 < nx) {
                    vx = elements[i+1][j];
                }
                if (j+1 < ny) {
                    vy = elements[i][j+1];
                }

                if (v != null && vx != null) {
                    v.coupleTo(vx, v.getAlongArea());
                }
                if (v != null && vy != null) {
                    v.coupleTo(vy, v.getTopArea());
                }
            }
        }
    }




    public void planeConnect(VolumeSlice tgt) {
        if (tgt.nx == nx && tgt.ny == ny) {
            // the easy case;
            for (int i = 0; i < nx; i++) {
                for (int j = 0; j < ny; j++) {
                    VolumeElement va = getElement(i, j);
                    VolumeElement vb = tgt.getElement(i, j);
                    if (va != null && vb != null) {
                        va.coupleTo(vb, va.getSideArea());
                    }
                }
            }

        } else {
            // not sure this is the right thing, but we should do it to see...
            // MISSING - just do as above for now...

            int nxu = (nx < tgt.nx ? nx : tgt.nx);
            int nyu = (ny < tgt.ny ? ny : tgt.ny);
            for (int i = 0; i < nxu; i++) {
                for (int j = 0; j < nyu; j++) {
                    VolumeElement va = getElement(i, j);
                    VolumeElement vb = tgt.getElement(i, j);
                    if (va != null && vb != null) {
                        va.coupleTo(vb, va.getTopArea());
                    }
                }
            }




        }
    }


    public ArrayList<VolumeElement> getElements() {
        ArrayList<VolumeElement> ave = new ArrayList<VolumeElement>();
        for (int i = 0; i < nx; i++) {
            for (int j = 0; j < ny; j++) {
                VolumeElement ve = getElement(i, j);
                if (ve != null) {
                    ave.add(ve);
                }
            }
        }
        return ave;
    }


}
