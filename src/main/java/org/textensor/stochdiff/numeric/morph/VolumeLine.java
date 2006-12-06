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
            VolumeElement ve = new VolumeElement();

            ve.setAlongArea(depth * sl);
            ve.setSideArea(depth * dl);

            Position cp = Geom.position(vcl, 0., 0.);
            Position pr = rot.getRotatedPosition(cp);
            Position pc = trans.getTranslated(pr);
            ve.setCenterPosition(pc.getX(), pc.getY(), pc.getZ());

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
            elements[i][0] = ve;
        }

        if (pointLabel != null) {
            elements[nl/2][0].setLabel(pointLabel);
        }

        neighborize();
    }



    public void neighborize() {
        for (int i = 0; i < nl; i++) {
            VolumeElement v = elements[i][0];
            VolumeElement vx = null;
            if (i+1 < nl) {
                vx = elements[i+1][0];
            }

            if (v != null && vx != null) {
                v.coupleTo(vx, v.getAlongArea());
            }
        }
    }



    public void planeConnect(VolumeLine tgt) {
        if (tgt.nl == nl) {
            // the easy case;
            for (int i = 0; i < nl; i++) {
                VolumeElement va = getElement(i);
                VolumeElement vb = tgt.getElement(i);
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

    private void smallBigMatchConnect(VolumeLine tgt) {
        // always have dlme <= dltgt, so at most two components in tgt
        // for one in me

        double[][] rngme = makeRanges(lSize, nl);
        double[][] rngtgt = makeRanges(tgt.lSize, tgt.nl);


        for (int i = 0; i < nl; i++) {
            VolumeElement va = getElement(i);
            int ifol = getFirstOverlap(rngme[i], rngtgt);

            double fol1 = overlapFactor(rngme[i], rngtgt[ifol]);
            if (fol1 > 0.001) {
                VolumeElement vb = tgt.getElement(ifol);

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
            // fully enclosed;
            ret = dr;
        } else if (rng[1] <= tgt[0] || rng[0] >= tgt[1]) {
            // no overlap
            ret = 0.;
        } else if (rng[1] < tgt[1]) {
            // overlaps lowe end of tgt only;
            ret = rng[1] - tgt[0];

        } else if (rng[1] >= tgt[1]) {
            // upper end of tgt;
            ret = tgt[1] - rng[0];
        }
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
