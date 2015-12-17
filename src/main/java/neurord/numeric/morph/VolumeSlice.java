package neurord.numeric.morph;

import neurord.geom.*;

import java.util.ArrayList;

public class VolumeSlice {

    final int nx;
    final int ny;
    double boxSize;
    double radius;


    final int icenter;
    final int jcenter;
    boolean[][] present;

    VolumeElement[][] elements;

    public VolumeSlice(double delta, double r) {
        boxSize = delta;
        radius = r;

        int nr = (int)(r / delta);
        int n = 1 + 2 * nr;
        this.nx = n;
        this.ny = n;
        this.icenter = nr;
        this.jcenter = nr;


        // work out which squares in the grid are going to be present as elements.
        // for a square section, just set all elements of present to true
        present = new boolean[this.nx][this.ny];
        int nt = 0;
        int nf = 0;
        for (int i = 0; i < this.nx; i++) {
            for (int j = 0; j < this.ny; j++) {
                double dx = (i - this.icenter) * boxSize;
                double dy = (j - this.jcenter) * boxSize;
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
        // E.info("created a volume slice " + this.nx + " by " + this.ny + " filling " + nt + " of " + (nt + nf));
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

        elements = new VolumeElement[this.nx][this.ny];


        // center of the box at 0,0
        double x0 = -1 * this.icenter * boxSize;
        double y0 = -1 * this.jcenter * boxSize;


        // this is a little confusing. X and Y axes are used within the slice, but when these are
        // turned into boxes, the slab of boxes is initially created in the X-Z plane before being rotated
        // into place


        for (int i = 0; i < this.nx; i++) {
            for (int j = 0; j < this.ny; j++) {

                if (present[i][j]) {

                    double vcx = x0 + i * boxSize;
                    double vcy =  y0 + j * boxSize;

                    final String label = i==this.icenter && j==this.jcenter ? pointLabel : null;

                    CuboidVolumeElement ve = new CuboidVolumeElement(label, regionLabel, null,
                                                                     boxSize * sl,
                                                                     boxSize * boxSize,
                                                                     boxSize * sl,
                                                                     boxSize * boxSize * sl, /* volume */
                                                                     boxSize);               /* deltaZ */
                    elements[i][j] = ve;

                    Position cp = Geom.position(vcx, vcy, 0.);
                    Position pr = rot.getRotatedPosition(cp);
                    Position pc = trans.getTranslated(pr);
                    ve.setCenterPosition(pc.getX(), pc.getY(), pc.getZ());

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

                    } else if (i == this.nx-1 || !present[i+1][j]) {
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

                    } else if (j == this.ny - 1 || !present[i][j+1]) {
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

        neighborize();
    }




    public void neighborize() {
        for (int i = 0; i < this.nx; i++) {
            for (int j = 0; j < this.ny; j++) {
                CuboidVolumeElement cv = (CuboidVolumeElement)elements[i][j];
                CuboidVolumeElement cvx = null;
                CuboidVolumeElement cvy = null;
                if (i+1 < this.nx) {
                    cvx = (CuboidVolumeElement)elements[i+1][j];
                }
                if (j+1 < this.ny) {
                    cvy = (CuboidVolumeElement)elements[i][j+1];
                }

                if (cv != null && cvx != null) {
                    cv.coupleTo(cvx, cv.getAlongArea());
                }
                if (cv != null && cvy != null) {
                    cv.coupleTo(cvy, cv.getTopArea());
                }
            }
        }
    }






    public void planeConnect(VolumeSlice tgt) {
        if (tgt.nx == this.nx && tgt.ny == this.ny) {
            // the easy case;
            for (int i = 0; i < this.nx; i++) {
                for (int j = 0; j < this.ny; j++) {
                    CuboidVolumeElement va = (CuboidVolumeElement)getElement(i, j);
                    CuboidVolumeElement vb = (CuboidVolumeElement)tgt.getElement(i, j);
                    if (va != null && vb != null) {
                        va.coupleTo(vb, va.getSideArea());
                    }
                }
            }

        } else {
            if (tgt.nx < this.nx) {
                tgt.planeConnectUp(this);
            } else {
                planeConnectUp(tgt);
            }
        }

    }

    private void planeConnectUp(VolumeSlice tgt) {
        // tgt is bigger than present slice;
        int io = (tgt.nx - this.nx) / 2;
        int jo = (tgt.ny - this.ny) / 2;

        for (int i = 0; i < this.nx; i++) {
            for (int j = 0; j < this.ny; j++) {
                CuboidVolumeElement va = (CuboidVolumeElement)getElement(i, j);
                CuboidVolumeElement vb = (CuboidVolumeElement)tgt.getElement(io + i, jo + j);
                if (va != null && vb != null) {
                    va.coupleTo(vb, va.getSideArea());
                }
            }
        }
    }


    public ArrayList<VolumeElement> getElements() {
        ArrayList<VolumeElement> ave = new ArrayList<VolumeElement>();
        for (int i = 0; i < this.nx; i++) {
            for (int j = 0; j < this.ny; j++) {
                VolumeElement ve = getElement(i, j);
                if (ve != null) {
                    ave.add(ve);
                }
            }
        }
        return ave;
    }


    public void subPlaneConnect(TreePoint tp, TreePoint tpn, VolumeSlice vg,
                                double pborel) {
        double pbo = 1 * (pborel - (radius - vg.radius));

        /*
        E.info("connecting to a subplane size " + vg.radius + " np=" + vg.nx +
                " offset by " + pbo);
        E.info("boxes and dims: " + boxSize + ", " + vg.boxSize + " " +
                this.nx + "," + vg.nx + "  rads " + radius + ", " + vg.radius);
        */

        // pbo is the offset in y to be applied to the target slice, vg

        /* we have nx, ny, xsize and radius
         * the target slice, vg has the same things
         *
         * need to couple our elements with the target slice elements where
         * they overlap with weight proportional to the fraction of our side
         * area that overlaps.
         *
         * compute target corners for all our elements
         * compute target corners for all elts in target slice, apply shift
         *
         * option 1: loop over target elts for each one of ours, compute overlap
         * if any, join.  - simple to implement, but slow
         *
         * option 2: for each target element, find the sorce elements in which
         * its corners lie and itereate over them only
         *
         *
         */


        double xtg0 = -1 * vg.icenter * vg.boxSize;
        double ytg0 = -1 * vg.jcenter * vg.boxSize + pbo;

        int ncpld = 0;

        for (int itg = 0; itg < vg.nx; itg++) {
            for (int jtg = 0; jtg < vg.ny; jtg++) {
                if (vg.hasElement(itg, jtg)) {
                    double cxtg = xtg0 + itg * vg.boxSize;
                    double cytg = ytg0 + jtg * vg.boxSize;

                    int ilmin = getIBox(cxtg);
                    int jlmin = getJBox(cytg);

                    int ilmax = getIBox(cxtg + vg.boxSize);
                    int jlmax = getJBox(cytg + vg.boxSize);

                    for (int il = ilmin; il <= ilmax; il++) {
                        for (int jl = jlmin; jl <= jlmax; jl++) {
                            if (hasElement(il, jl)) {

                                double ovlp = overlapFactor(getX(il), getY(il), boxSize,
                                                            cxtg, cytg, vg.boxSize);
                                if (ovlp > 0.) {

                                    getElement(il,jl).coupleTo(vg.getElement(itg, jtg),
                                                               ovlp * boxSize * boxSize);
                                    ncpld += 1;
                                }
                            }
                        }
                    }

                }

            }
        }
        // E.info("coupled " + ncpld + " elements ");
    }

    private double overlapFactor(double mex, double mey, double med,
                                 double tgtx, double tgty, double tgtd) {

        double fx = ovlp1D(mex, med, tgtx, tgtd);
        double fy = ovlp1D(mey, med, tgty, tgtd);
        double ret = fx * fy;
        return ret;
    }

    private double ovlp1D(double mex, double med, double tgtx, double tgtd) {
        double ret = 0.;
        if (mex <= tgtx) {
            if (mex + med > tgtx + tgtd) {
                ret = tgtd;
            } else {
                ret = mex + med - tgtx;
            }
        } else {
            if (mex + med > tgtx + tgtd) {
                ret = med;
            } else {
                ret = tgtx + tgtd - mex;
            }
        }
        return ret / med;
    }

    public double getX(int i) {
        double ret = (-this.nx/2. + i) * boxSize;
        return ret;
    }


    public double getY(int j) {
        double ret = (-this.ny/2. + j) * boxSize;
        return ret;
    }

    public boolean hasElement(int i, int j) {
        boolean ret = false;
        if (i >= 0 && i < this.nx && j >= 0 && j < this.ny) {
            ret = present[i][j];
        }
        return ret;
    }

    public int getIBox(double x) {
        int ret = (int)(x / boxSize + this.nx / 2.);
        return ret;
    }

    public int getJBox(double y) {
        int ret = (int)(y / boxSize + this.ny / 2.);
        return ret;
    }
}
