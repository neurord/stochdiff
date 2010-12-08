//9 12 2008: WK added ret /= dr; before the return statement in the overlapFactor function per RC.
//6 18 2007: WK added "ve.setSubmembrane()" to the lineFill function
//written by Robert Cannon
package org.textensor.stochdiff.numeric.morph;

/*
 * A line of cuboid volume elements across the diameter of dendrite.
 * Used for producing 2D models
 */



import org.textensor.report.E;
import org.textensor.stochdiff.geom.*;

import java.util.ArrayList;

public class VolumeLine {

    int nl;
    double lSize;
    double depth;

    VolumeElement[][] elements;

    public VolumeLine(int n, double w, double d) {
        nl = n;
        lSize = w;
        depth = d;
    }


    public VolumeElement getElement(int i) {
        return elements[i][0];
    }


    public void lineFill(Position pa, Position pb,
                         String pointLabel, String regionLabel) {
        double sl = Geom.distanceBetween(pa, pb);

        Translation trans = Geom.translation(Geom.midpoint(pa, pb));
        Vector vab = Geom.fromToVector(pa, pb);
        double theta = Geom.zRotationAngle(Geom.unitY(), vab);
        Rotation rot = Geom.aboutZRotation(theta);

        elements = new VolumeElement[nl][1];

        double dl = lSize / nl;
        for (int i = 0; i < nl; i++) {
            double vcl = -0.5 * lSize + (i + 0.5) * dl;
            CuboidVolumeElement ve = new CuboidVolumeElement();

            ve.setAlongArea(depth * sl);
            ve.setSideArea(depth * dl);

            Position cp = Geom.position(vcl, 0., 0.);
            Position pr = rot.getRotatedPosition(cp);
            Position pc = trans.getTranslated(pr);
            ve.setCenterPosition(pc.getX(), pc.getY(), pc.getZ());

            //<--WK
            if ((i == 0) || (i == (nl-1)))
                ve.setSubmembrane();
            //WK-->

            Position[] pbdry = {Geom.position(vcl - 0.5 * dl, -0.5 * sl, 0),
                                Geom.position(vcl - 0.5 * dl, 0.5 * sl, 0),
                                Geom.position(vcl + 0.5 * dl, 0.5 * sl, 0),
                                Geom.position(vcl + 0.5 * dl, -0.5 * sl, 0)
                               };

            for (int ib = 0; ib < pbdry.length; ib++) {
                pbdry[ib] = trans.getTranslated(rot.getRotatedPosition(pbdry[ib]));
            }

            ve.setBoundary(pbdry);


            if (regionLabel != null) {
                ve.setRegion(regionLabel);
            }


            if (i == 0 || i == nl-1) {
                Position[] psb = new Position[4];
                if (i == 0) {
                    double xb  = vcl - 0.5 * dl;
                    psb[0] = Geom.position(xb, -0.5 * sl, -0.5*depth);
                    psb[1] = Geom.position(xb, -0.5 * sl, 0.5*depth);
                    psb[2] = Geom.position(xb, 0.5 * sl, 0.5*depth);
                    psb[3] = Geom.position(xb, 0.5 * sl, -0.5*depth);

                } else {
                    double xb =  vcl + 0.5 * dl;
                    psb[0] = Geom.position(xb, -0.5 * sl, -0.5*depth);
                    psb[1] = Geom.position(xb, 0.5 * sl, -0.5*depth);
                    psb[2] = Geom.position(xb, 0.5 * sl, 0.5*depth);
                    psb[3] = Geom.position(xb, -0.5 * sl, 0.5*depth);
                }

                for (int ib = 0; ib < psb.length; ib++) {
                    psb[ib] = trans.getTranslated(rot.getRotatedPosition(psb[ib]));
                }
                ve.setSurfaceBoundary(psb);
                ve.setExposedArea(sl * depth);
            }


            ve.setVolume(dl * sl * depth);
            ve.setDeltaZ(depth);
            elements[i][0] = ve;
        }

        if (pointLabel != null) {
            elements[nl/2][0].setLabel(pointLabel);
        }

        neighborize();
    }



    public void neighborize() {
        for (int i = 0; i < nl; i++) {
            CuboidVolumeElement cv = (CuboidVolumeElement)elements[i][0];
            CuboidVolumeElement cvx = null;
            if (i+1 < nl) {
                cvx = (CuboidVolumeElement)elements[i+1][0];
            }

            if (cv != null && cvx != null) {
                cv.coupleTo(cvx, cv.getAlongArea());
            }
        }
    }



    public void planeConnect(VolumeLine tgt) {
        if (tgt.nl == nl) {
            // the easy case;
            for (int i = 0; i < nl; i++) {
                CuboidVolumeElement va = (CuboidVolumeElement)getElement(i);
                CuboidVolumeElement vb = (CuboidVolumeElement)tgt.getElement(i);
                if (va != null && vb != null) {
                    va.coupleTo(vb, va.getSideArea());
                }
            }


        } else {
            if (lSize / nl <= tgt.lSize / tgt.nl) {
                smallBigMatchConnect(tgt);
            } else {
                tgt.smallBigMatchConnect(this);
            }
        }
    }


    public void subPlaneConnect(TreePoint tp, TreePoint tpl, VolumeLine tgt,
                                double offset) {

        E.info("segment junction: " + nl + " points across " + lSize +
               " being connected to " + tgt.nl + " across " + tgt.lSize);

        double[][] rngme = makeRanges(lSize, nl);
        double[][] rngtgt = makeRanges(tgt.lSize, tgt.nl);

        // these are centered on 0, so we need to shift the smaller one
        // back so the edges line up and then move it by the current
        // offset
        // -1 here because we measure from the "bottom" of the parent segment
        double offeff = -1 * (offset - 0.5 * (lSize - tgt.lSize));
        E.info("shifting child branch " + offeff + " relative to parent center");

        // E.info("effoff " + offset + " " + lSize + " " + tgt.lSize + " " + offeff);
        for (int i = 0; i < rngtgt.length; i++) {
            rngtgt[i][0] += offeff;
            rngtgt[i][1] += offeff;
        }

        /*
        for (int i = 0; i < rngme.length; i++) {
           E.info("rngme " + rngme[i][0] + " to " + rngme[i][1]);
        }
        for (int i = 0; i < rngtgt.length; i++) {
           E.info("rngtgt " + rngtgt[i][0] + " to " + rngtgt[i][1]);
        }
        */

        for (int i = 0; i < tgt.nl; i++) {
            VolumeElement vtgt = tgt.getElement(i);

            for (int jme = 0; jme < nl; jme++) {
                double fol = overlapFactor(rngme[jme], rngtgt[i]);
                if (fol > 0.001) {
                    CuboidVolumeElement vme = (CuboidVolumeElement)getElement(jme);
                    vme.coupleTo(vtgt, fol * vme.getSideArea());
                    E.info("coupled parent element " + jme + " to child element " + i + " overlap factor = " + fol);
                }
            }
        }

        // NB this is second order in the number of elements across a segment,
        // which could get slow if there are lots (hundreds). Then it could be
        // worth being a bit smarter and walking through keeping track of the
        // position and moving relative to that.
        // But this method is also only used on branches to smaller elements
        // so it doesn't get called a lot.

    }






    private void smallBigMatchConnect(VolumeLine tgt) {
        // always have dlme <= dltgt, so at most two components in tgt
        // for one in me

        double[][] rngme = makeRanges(lSize, nl);
        double[][] rngtgt = makeRanges(tgt.lSize, tgt.nl);


        for (int i = 0; i < nl; i++) {
            CuboidVolumeElement va = (CuboidVolumeElement)getElement(i);
            int ifol = getFirstOverlap(rngme[i], rngtgt);

            double fol1 = overlapFactor(rngme[i], rngtgt[ifol]);
            if (fol1 > 0.001) {
                CuboidVolumeElement vb = (CuboidVolumeElement)tgt.getElement(ifol);

                va.coupleTo(vb, fol1 * va.getSideArea());

            } else {
                // E.info("not coupling " + i + " " + ifol + " " + fol1);
            }

            if (ifol < rngtgt.length - 1) {
                double fol2 = overlapFactor(rngme[i], rngtgt[ifol+1]);
                if (fol2 > 0.001) {
                    VolumeElement vb = tgt.getElement(ifol + 1);
                    va.coupleTo(vb, fol2 * va.getSideArea());

                } else {
                    // dont couple...
                }
            }
        }
    }




    public int getFirstOverlap(double[] rng, double[][] tgts) {
        int iol = 0;
        while (iol < tgts.length-1 && (rng[1] <= tgts[iol][0] || rng[0] >= tgts[iol][1])) {
            iol += 1;
        }
        return iol;
    }

    private double overlapFactor(double[] rng, double[] tgt) {
        double dr = rng[1] - rng[0];
        double ret = 0.;
        if (rng[0] >= tgt[0] && rng[1] <= tgt[1]) {
            // fully enclosed: rng is within tgt;
            ret = dr;
        } else if (rng[1] <= tgt[0] || rng[0] >= tgt[1]) {
            // no overlap
            ret = 0.;
        } else if (rng[1] < tgt[1]) {
            // overlaps lowe end of tgt only;
            ret = rng[1] - tgt[0];

        } else if (rng[1] >= tgt[1]) {
            if (rng[0] < tgt[0]) {
                // fully enclosed: tgt is within rng;
                ret = tgt[1] - tgt[0];
            } else {
                // upper end of tgt;
                ret = tgt[1] - rng[0];
            }
        }
//    <--WK
        ret /= dr;
//    WK-->
        return ret;
    }



    private double[][] makeRanges(double ltot, int nreg) {
        double[][] ret = new double[nreg][2];
        double dl = ltot / nreg;
        for (int i = 0; i < nreg; i++) {
            double rl = -0.5 * ltot + i * dl;
            ret[i][0] = rl;
            ret[i][1] = rl + dl;
        }
        return ret;
    }



    public ArrayList<VolumeElement> getElements() {
        ArrayList<VolumeElement> ave = new ArrayList<VolumeElement>();
        for (int i = 0; i < nl; i++) {
            VolumeElement ve = getElement(i);
            if (ve != null) {
                ave.add(ve);
            }

        }
        return ave;
    }


}
