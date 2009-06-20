package org.textensor.stochdiff.numeric.morph;

import org.textensor.report.E;
import org.textensor.stochdiff.geom.*;

import java.util.ArrayList;

public class VolumeSlice {

    int nx;
    int ny;
    double boxSize;
    double radius;
    double xSize;
    double ySize;

    int icenter;
    int jcenter;
    boolean[][] present;

    VolumeElement[][] elements;

    public VolumeSlice(double delta, double r) {
        boxSize = delta;
        radius = r;

        int nr = (int)(r / delta);
        int n = 1 + 2 * nr;
        nx = n;
        ny = n;
        icenter = nr;
        jcenter = nr;


        // work out which squares in the grid are going to be present as elements.
        // for a square section, just set all elements of present to true
        present = new boolean[nx][ny];
        int nt = 0;
        int nf = 0;
        for (int i = 0; i < nx; i++) {
            for (int j = 0; j < ny; j++) {
                double dx = (i - icenter) * boxSize;
                double dy = (j - jcenter) * boxSize;
                double r2 = dx * dx + dy * dy;
                if (r2 < radius * radius) {
                    present[i][j] = true;
                    nt += 1;
                } else {
                    present[i][j] = false;
                    nf += 1;
                }
            }
        }
        E.info("created a volume slice " + nx + " by " + ny + " filling " + nt + " of " + (nt + nf));
    }


    public VolumeElement getElement(int i, int j) {
        return elements[i][j];
    }


    public void discFill(Position pa, Position pb, String pointLabel, String regionLabel) {

        double sl = Geom.distanceBetween(pa, pb);
        Translation trans = Geom.translation(Geom.midpoint(pa, pb));
        Vector vab = Geom.fromToVector(pa, pb);
        double theta = Geom.zRotationAngle(Geom.unitY(), vab);
        Rotation rot = Geom.aboutZRotation(theta);

        elements = new VolumeElement[nx][ny];


        // center of the box at 0,0
        double x0 = -1 * icenter * boxSize;
        double y0 = -1 * icenter * boxSize;


        // this is a little confusing. X and Y axes are used within the slice, but when these are
        // turned into boxes, the slab of boxes is initially created in the X-Z plane before being rotated
        // into place


        for (int i = 0; i < nx; i++) {
            for (int j = 0; j < ny; j++) {

                if (present[i][j]) {

                    double vcx = x0 + i * boxSize;
                    double vcy =  y0 + j * boxSize;

                    VolumeElement ve = new VolumeElement();
                    elements[i][j] = ve;
                    if (regionLabel != null) {
                        ve.setRegion(regionLabel);
                    }
                    ve.setVolume(boxSize * boxSize * sl);

                    Position cp = Geom.position(vcx, vcy, 0.);
                    Position pr = rot.getRotatedPosition(cp);
                    Position pc = trans.getTranslated(pr);
                    ve.setCenterPosition(pc.getX(), pc.getY(), pc.getZ());


                    ve.setAlongArea(boxSize * sl);
                    ve.setSideArea(boxSize * boxSize);
                    ve.setTopArea(boxSize * sl);


                    // this is the boundary of a slice through the box perpendicular to the z axis
                    // it is not used for the computation, just for visualization
                    Position[] pbdry = {Geom.position(vcx - 0.5 * boxSize, -0.5 * sl, vcy),
                                        Geom.position(vcx - 0.5 * boxSize, 0.5 * sl, vcy),
                                        Geom.position(vcx + 0.5 * boxSize, 0.5 * sl, vcy),
                                        Geom.position(vcx + 0.5 * boxSize, -0.5 * sl, vcy)
                                       };

                    for (int ib = 0; ib < pbdry.length; ib++) {
                        pbdry[ib] = trans.getTranslated(rot.getRotatedPosition(pbdry[ib]));
                    }
                    ve.setBoundary(pbdry);


                    if (regionLabel != null) {
                        ve.setRegion(regionLabel);
                    }


                    boolean surf = false;
                    double hb = 0.5 * boxSize;
                    Position[] psb = new Position[4];
                    // four different cases here since the boundary points have to go in the right order to give
                    // the right-hand normal pointing outwards
                    if (i == 0 || !present[i-1][j]) {
                        surf = true;
                        double xb = vcx + -0.5 * boxSize;
                        psb[0] = Geom.position(xb, -0.5 * sl, vcy - hb);
                        psb[1] = Geom.position(xb, -0.5 * sl, vcy + hb);
                        psb[2] = Geom.position(xb, 0.5 * sl, vcy + hb);
                        psb[3] = Geom.position(xb, 0.5 * sl, vcy - hb);

                    } else if (i == nx-1 || !present[i+1][j]) {
                        surf = true;
                        double xb = vcx + 0.5 * boxSize;
                        psb[0] = Geom.position(xb, -0.5 * sl, vcy + hb);
                        psb[1] = Geom.position(xb, -0.5 * sl, vcy - hb);
                        psb[2] = Geom.position(xb, 0.5 * sl, vcy - hb);
                        psb[3] = Geom.position(xb, 0.5 * sl, vcy + hb);

                    } else if (j == 0 || !present[i][j-1]) {
                        surf = true;
                        double yb = vcy - 0.5 * boxSize;
                        psb[0] = Geom.position(vcx + hb, -0.5 * sl, yb);
                        psb[1] = Geom.position(vcx - hb, -0.5 * sl, yb);
                        psb[2] = Geom.position(vcx - hb, 0.5 * sl, yb);
                        psb[3] = Geom.position(vcx + hb, 0.5 * sl, yb);

                    } else if (j == ny - 1 || !present[i][j+1]) {
                        surf = true;
                        double yb = vcy + 0.5 * boxSize;
                        psb[0] = Geom.position(vcx - hb, -0.5 * sl, yb);
                        psb[1] = Geom.position(vcx + hb, -0.5 * sl, yb);
                        psb[2] = Geom.position(vcx + hb, 0.5 * sl, yb);
                        psb[3] = Geom.position(vcx - hb, 0.5 * sl, yb);
                    }

                    if (surf) {
                        ve.setSubmembrane();

                        for (int ib = 0; ib < psb.length; ib++) {
                            psb[ib] = trans.getTranslated(rot.getRotatedPosition(psb[ib]));
                        }
                        ve.setSurfaceBoundary(psb);
                        ve.setExposedArea(sl * boxSize);
                    }
                }

            }
        }

        if (pointLabel != null) {
            elements[icenter][icenter].setLabel(pointLabel);
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
            if (tgt.nx < nx) {
                tgt.planeConnectUp(this);
            } else {
                planeConnectUp(tgt);
            }
        }

    }

    private void planeConnectUp(VolumeSlice tgt) {
        // tgt is bigger than present slice;
        int io = (tgt.nx - nx) / 2;
        int jo = (tgt.ny - ny) / 2;

        for (int i = 0; i < nx; i++) {
            for (int j = 0; j < ny; j++) {
                VolumeElement va = getElement(i, j);
                VolumeElement vb = tgt.getElement(io + i, jo + j);
                if (va != null && vb != null) {
                    va.coupleTo(vb, va.getSideArea());
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
