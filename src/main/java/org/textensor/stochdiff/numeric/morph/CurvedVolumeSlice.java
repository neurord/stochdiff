package org.textensor.stochdiff.numeric.morph;

import org.textensor.report.E;
import org.textensor.stochdiff.geom.*;

import java.util.ArrayList;

public class CurvedVolumeSlice {

    double baseDelta;
    double radius1;
    double radius2;

    ArrayList<VolumeElement> elements;


    public CurvedVolumeSlice(double delta, double r1, double r2) {
        baseDelta = delta;
        radius1 = r1;
        radius2 = r2;
    }



    public void discFill(Position pa, Position pb, String pointLabel, String regionLabel,
                         boolean hasSurfaceLayer, double slDepth, double maxAR) {

        double sl = Geom.distanceBetween(pa, pb);
        Translation trans = Geom.translation(Geom.midpoint(pa, pb));
        Vector vab = Geom.fromToVector(pa, pb);
        double theta = Geom.zRotationAngle(Geom.unitY(), vab);
        Rotation rot = Geom.aboutZRotation(theta);

        elements = new ArrayList<VolumeElement>();


        // center of the box at 0,0


        // this is a little confusing. X and Y axes are used within the slice, but when these are
        // turned into boxes, the slab of boxes is initially created in the X-Z plane before being rotated
        // into place

        double maxr = Math.max(radius1, radius2);
        double[] bdm = getRadialSplit(maxr, hasSurfaceLayer, slDepth);

        double[] bds1 = getRadialSplit(radius1, bdm.length, hasSurfaceLayer, slDepth);
        double[] bds2 = getRadialSplit(radius2, bdm.length, hasSurfaceLayer, slDepth);


        int[] nazim = getAzimuthalSplits(maxr, bdm);


    }




    // TODO need a main method with some tests of getRadialSplit

    private int[] getAzimuthalSplits(double maxr, double[] bdm) {
        // MUSTDO Auto-generated method stub
        return null;
    }



    private double[] getRadialSplit(double r, boolean hsl, double sld) {
        return getRadialSplit(r, 0, hsl, sld);
    }


    private double[] getRadialSplit(double r, int ansplit, boolean hsl, double sld) {
        double[] ret = null;
        int nsplit = ansplit;
        if (hsl) {
            if (r < sld) {
                ret = new double[1];
                ret[0] = r;
            } else {
                int nr = 1;
                if (ansplit > 0) {
                    nr = ansplit - 1;
                } else {
                    nr = (int)Math.round((r - sld) / baseDelta);
                    if (nr < 1) {
                        nr = 1;
                    }
                }
                ret = new double[nr + 1];
                for (int i = 0; i < nr+1; i++) {
                    ret[i] = ((i + 1.)/(nr)) * (r - sld);
                }
                ret[nr] = r;
            }


        } else {
            if (nsplit > 0) {
                ret = new double[nsplit];
            } else {
                nsplit = (int)Math.round(r / baseDelta);
                if (nsplit < 1) {
                    nsplit = 1;
                }
            }
            for (int i = 0; i < nsplit; i++) {
                ret[i] = ((i + 1.)/ nsplit) * r;
            }
        }

        return ret;
    }








    public void planeConnect(CurvedVolumeSlice vg) {
        // TODO Auto-generated method stub

    }



    public void subPlaneConnect(TreePoint tp, TreePoint tpn, CurvedVolumeSlice vg, double partBranchOffset) {
        // TODO Auto-generated method stub

    }



    /*
           double vcx = x0 + i * boxSize;
           double vcy =  y0 + j * boxSize;

           VolumeElement ve = new VolumeElement();
           elements[i][j] = ve;
           if (regionLabel != null) {
              ve.setRegion(regionLabel);
           }
           ve.setVolume(boxSize * boxSize * sl);
           ve.setDeltaZ(boxSize);

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
                 Geom.position(vcx + 0.5 * boxSize, -0.5 * sl, vcy)};

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


    if (pointLabel != null) {
     elements[icenter][icenter].setLabel(pointLabel);
    }
    neighborize();

    // neighborize calls v.coupleTo(vnbr, area-of-contact);
    */











}
