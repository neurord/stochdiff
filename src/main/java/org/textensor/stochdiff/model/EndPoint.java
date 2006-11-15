package org.textensor.stochdiff.model;

import java.util.HashMap;


import org.textensor.report.E;


public class EndPoint extends MorphPoint {

    public String on;
    public String at;
    public double atFraction;



    public EndPoint() {
        super();
        atFraction = -1.;

    }



    public void resolve(HashMap<String, Segment> segmentHM, MorphPoint ep) {
        if (on != null) {
            if (segmentHM.containsKey(on)) {
                Segment tgtSeg = segmentHM.get(on);
                if (at != null) {
                    if (at.equals("start")) {
                        pointConnect(tgtSeg.getStart());

                    } else if (at.equals("end")) {
                        pointConnect(tgtSeg.getEnd());

                    } else {
                        E.error("connection to segment: at value can only be 'start' or 'end', not " + at);
                    }

                } else {
                    if (atFraction < 0.) {
                        E.error("must either set 'at' or 'atFraction' if 'on' is specified");

                    } else if (atFraction < 1.e-4) {
                        pointConnect(tgtSeg.getStart());

                    } else if (atFraction > 1. - 1.e-4) {
                        pointConnect(tgtSeg.getEnd());

                    } else {
                        tgtSeg.checkResolved(segmentHM);
                        tgtSeg.checkHasPositions();

                        interiorPointConnect(tgtSeg, atFraction, ep);

                    }

                }

            } else {
                E.error("point refers to segment " + on + " but that segment cant be found");
            }

        }

    }


    private void supplySize(double xp, double yp, double zp, double rp) {
        if (Double.isNaN(x)) {
            x = xp;
        }
        if (Double.isNaN(y)) {
            y = yp;
        }
        if (Double.isNaN(z)) {
            z = zp;
        }
        if (Double.isNaN(r)) {
            r = rp;
        }
    }


    public boolean radiiDiffer(MorphPoint mp) {
        boolean ret = true;
        if (Math.abs((r - mp.r) / (r + mp.r)) < 1.e-4) {
            ret = false;
        }
        return ret;
    }


    private void pointConnect(MorphPoint tgtEP) {
        supplySize(tgtEP.x, tgtEP.y, tgtEP.z, tgtEP.r);

        if (radiiDiffer(tgtEP)) {
            tgtEP.addNeighbor(this);
            addNeighbor(tgtEP);

        } else {
            r_peerPoint = tgtEP;
        }
    }




    public void interiorPointConnect(Segment tgtSeg, double f, MorphPoint twds) {
        MorphPoint st = tgtSeg.getStart();
        MorphPoint ed = tgtSeg.getEnd();


        double cx = 0.5 * (st.x + ed.x);
        double cy = 0.5 * (st.y + ed.y);
        double cz = 0.5 * (st.z + ed.z);
        double cr = 0.5 * (st.r + ed.r);

        //  double rr = (Double.isNaN(r) ? cr : r);

        // the attacheent position should be offset cr towards the end of the
        // new segment;
        double dx = (twds.x -cx);
        double dy = (twds.y -cy);
        double dz = (twds.z -cz);
        double d = Math.sqrt(dx*dx + dy*dy + dz*dz);

        supplySize(cx + cr * dx/d, cy + cr * dy/d, cz * cr * dz/d, cr);

        st.addOffsetChild(this);
    }


    public String writePos() {
        return "(x=" + x + ", y=" + y + ", z=" + z +")";
    }






}
