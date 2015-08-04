package org.textensor.stochdiff.model;

import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.bind.annotation.*;

import org.textensor.stochdiff.numeric.morph.TreePoint;
import org.textensor.util.inst;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class MorphPoint {
    static final Logger log = LogManager.getLogger();

    @XmlAttribute public double x;
    @XmlAttribute public double y;
    @XmlAttribute public double z;
    @XmlAttribute public double r;
    @XmlAttribute public String label;


    transient private Segment r_segment;

    transient protected MorphPoint r_peerPoint;

    transient private ArrayList<MorphPoint> neighbors;

    transient private ArrayList<MorphPoint> offsetNeighbors;


    transient HashMap<MorphPoint, String> segidHM = inst.newHashMap();
    transient HashMap<MorphPoint, String> regionHM = inst.newHashMap();

    public MorphPoint() {
        x = y = z = r = Double.NaN;
    }

    public MorphPoint(String label, double x, double y, double z, double r) {
        this.label = label;
        this.x = x;
        this.y = y;
        this.z = z;
        this.r = r;
    }

    public void setSegment(Segment seg) {
        r_segment = seg;

    }

    protected void addNeighbor(MorphPoint p) {
        if (neighbors == null)
            neighbors = new ArrayList<MorphPoint>();

        neighbors.add(p);
    }


    // these are branches that start some way down a segment, but are
    // linked from here temporarily until the tree is discretized and a new point
    // is available to have them connected from as neighbors
    public void addOffsetChild(MorphPoint p) {
        if (offsetNeighbors == null)
            offsetNeighbors = new ArrayList<MorphPoint>();

        offsetNeighbors.add(p);
    }

    public boolean hasOffsetChildren() {
        return offsetNeighbors != null;
    }

    public ArrayList<MorphPoint> getOffsetChildren() {
        return offsetNeighbors;
    }

    public void removeNeighbor(MorphPoint mp) {
        neighbors.remove(mp);
    }

    public boolean redundant() {
        return r_peerPoint != null;
    }

    public ArrayList<MorphPoint> getNeighbors() {
        return neighbors;
    }

    public TreePoint toTreePoint() {
        TreePoint tp = new TreePoint(x, y, z, r);
        if (label != null)
            tp.setLabel(label);

        return tp;
    }

    public void transferConnections() {
        if (r_peerPoint == null) {
            log.error("cannot transfer connections - no peer");
            throw new RuntimeException("cannot transfer connections - no peer");
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
                for (MorphPoint mon : offsetNeighbors)
                    r_peerPoint.addOffsetChild(mon);
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
        if (!hasPosition() || !mp.hasPosition()) {
            log.error("cannot calculate distance (undefined position)");
            throw new RuntimeException("cannot calculate distance (undefined position)");
        }

        double dx = mp.x - x;
        double dy = mp.y - y;
        double dz = mp.z - z;
        return Math.sqrt(dx*dx + dy*dy + dz*dz);
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
