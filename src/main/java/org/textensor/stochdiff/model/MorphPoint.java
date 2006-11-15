package org.textensor.stochdiff.model;

import java.util.ArrayList;

import org.textensor.report.E;
import org.textensor.stochdiff.numeric.morph.TreePoint;

import java.util.HashMap;


public class MorphPoint {

    public String label;
    public double x;
    public double y;
    public double z;
    public double r;

    // bot sloppy - coud be at boundary of two of up to three regins;
    public String region;
    public String region1;
    public String region2;

    private Segment r_segment;

    protected MorphPoint r_peerPoint;

    private ArrayList<MorphPoint> neighbors;

    private ArrayList<MorphPoint> offsetNeighbors;


    public HashMap<MorphPoint, String> segidHM;
    public HashMap<MorphPoint, String> regionHM;




    public MorphPoint() {
        x = Double.NaN;
        y = Double.NaN;
        z = Double.NaN;
        r = Double.NaN;

        segidHM = new HashMap<MorphPoint, String>();
        regionHM = new HashMap<MorphPoint, String>();

    }

    public void setSegment(Segment seg) {
        r_segment = seg;

    }

    public void supplyRegion(String s) {
        if (region == null || region == "" || region.equals(s)) {
            region = s;
        } else if (region1 == null || region1 == "" || region1.equals(s)) {
            region1 = s;

        } else if (region2 == null || region2 == "" || region2.equals(s)) {
            region2 = s;
        }

        if (r_peerPoint != null) {
            r_peerPoint.supplyRegion(s);
        }
    }



    protected void addNeighbor(MorphPoint p) {
        if (neighbors == null) {
            neighbors = new ArrayList<MorphPoint>();
        }
        neighbors.add(p);
    }


    // these are branches that start some way down a segment, but are
    // linked from here temporarily until the tree is discretized and a new point
    // is available to have them connected from as neighbors
    public void addOffsetChild(MorphPoint p) {
        if (offsetNeighbors == null) {
            offsetNeighbors = new ArrayList<MorphPoint>();
        }
        offsetNeighbors.add(p);
    }

    public boolean hasOffsetChildren() {
        return (offsetNeighbors != null);
    }

    public ArrayList<MorphPoint> getOffsetChildren() {
        return offsetNeighbors;
    }




    public void removeNeighbor(MorphPoint mp) {
        neighbors.remove(mp);
    }


    public boolean redundant() {
        return (r_peerPoint != null);
    }


    public ArrayList<MorphPoint> getNeighbors() {
        return neighbors;
    }

    public TreePoint toTreePoint() {
        TreePoint tp = new TreePoint(x, y, z, r);
        if (region != null) {
            tp.setRegion(region);
            if (region1 != null) {
                tp.setRegion1(region1);
                if (region2 != null) {
                    tp.setRegion2(region2);
                }
            }

        }
        if (label != null) {
            tp.setLabel(label);
        }
        return tp;
    }

    public void transferConnections() {
        if (r_peerPoint == null) {
            E.error("cant transfer connections - no peer");
        } else {
            if (neighbors != null) {
                for (MorphPoint mp : neighbors) {
                    mp.removeNeighbor(this);
                    r_peerPoint.addNeighbor(mp);
                    mp.addNeighbor(r_peerPoint);

                    if (segidHM.containsKey(mp)) {
                        r_peerPoint.setIDWith(mp, segidHM.get(mp));
                    }
                    if (regionHM.containsKey(mp)) {
                        r_peerPoint.setRegionWith(mp, regionHM.get(mp));
                    }

                    mp.replaceNeighborLabels(this, r_peerPoint);
                }
                neighbors = null;
            }

            if (offsetNeighbors != null) {
                for (MorphPoint mon : offsetNeighbors) {
                    r_peerPoint.addOffsetChild(mon);
                }
                offsetNeighbors = null;
            }
        }

    }


    public boolean hasPosition() {
        boolean ret = true;
        if (Double.isNaN(x) || Double.isNaN(y) || Double.isNaN(z)) {
            ret = false;
        }
        return ret;
    }


    public double distanceTo(MorphPoint mp) {
        double ret = 1.;
        if (hasPosition() && mp.hasPosition()) {
            double dx = mp.x - x;
            double dy = mp.y - y;
            double dz = mp.z - z;
            ret = Math.sqrt(dx*dx + dy*dy + dz*dz);

        } else {
            E.error("cant calculate distance (undefined position)");
        }
        return ret;
    }


    public void setIDWith(MorphPoint end, String id) {
        segidHM.put(end, id);
    }


    public void setRegionWith(MorphPoint ep, String r) {
        regionHM.put(ep, r);
    }



    public void replaceNeighborLabels(MorphPoint mp, MorphPoint mpnew) {
        if (segidHM.containsKey(mp)) {
            segidHM.put(mpnew, segidHM.get(mp));
            segidHM.remove(mp);
        }
        if (regionHM.containsKey(mp)) {
            regionHM.put(mpnew, regionHM.get(mp));
            regionHM.remove(mp);
        }
    }

}
