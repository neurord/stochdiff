package org.textensor.stochdiff.numeric.morph;

/*
 * A line of cuboid volume elements across the diameter of dendrite.
 * Used for producing 2D models
 */



import org.textensor.stochdiff.geom.*;

import java.util.ArrayList;

public class VolumeLine {

    int nl;
    double lSize;

    VolumeElement[][] elements;

    public VolumeLine(int n, double w) {
        nl = n;
        lSize = w;
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

            ve.setAlongArea(dl * sl); // POSERR - what height do we want?
            ve.setSideArea(dl * dl);

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
                    psb[0] = Geom.position(xb, -0.5 * sl, -0.5*dl);
                    psb[1] = Geom.position(xb, -0.5 * sl, 0.5*dl);
                    psb[2] = Geom.position(xb, 0.5 * sl, 0.5*dl);
                    psb[3] = Geom.position(xb, 0.5 * sl, -0.5*dl);

                } else {
                    double xb =  vcl + 0.5 * dl;
                    psb[0] = Geom.position(xb, -0.5 * sl, -0.5*dl);
                    psb[1] = Geom.position(xb, 0.5 * sl, -0.5*dl);
                    psb[2] = Geom.position(xb, 0.5 * sl, 0.5*dl);
                    psb[3] = Geom.position(xb, -0.5 * sl, 0.5*dl);
                }

                for (int ib = 0; ib < psb.length; ib++) {
                    psb[ib] = trans.getTranslated(rot.getRotatedPosition(psb[ib]));
                }
                ve.setSurfaceBoundary(psb);
            }


            ve.setVolume(dl * dl * sl);  // TODO - different height?
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
            // not sure this is the right thing, but we should do it to see...
            // MISSING - just do as above for now...

            int nlu = (nl < tgt.nl ? nl : tgt.nl);
            for (int i = 0; i < nlu; i++) {
                VolumeElement va = getElement(i);
                VolumeElement vb = tgt.getElement(i);
                if (va != null && vb != null) {
                    va.coupleTo(vb, va.getSideArea());
                }

            }

        }
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
