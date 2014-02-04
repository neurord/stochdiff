package org.textensor.stochdiff.model;

import java.util.HashMap;

import javax.xml.bind.annotation.*;

import org.textensor.report.E;

public class Segment {

    @XmlAttribute public String id;
    @XmlAttribute public String region;

    public EndPoint start;
    public EndPoint end;

    private transient boolean resolved = false;

    public String getID() {
        return id;
    }

    public void checkResolved(HashMap<String, Segment> segmentHM) {
        if (!resolved)
            resolve(segmentHM);
    }

    public void resolve(HashMap<String, Segment> segmentHM) {
        start.setSegment(this);
        end.setSegment(this);

        start.resolve(segmentHM, end);
        end.resolve(segmentHM, start);


        start.addNeighbor(end);
        end.addNeighbor(start);

        if (region != null) {
            start.setRegionWith(end, region);
            end.setRegionWith(start, region);
        }

        if (id != null) {
            start.setIDWith(end, id);
            end.setIDWith(start, id);
        }
        resolved = true;
    }

    public EndPoint getStart() {
        return start;
    }

    public EndPoint getEnd() {
        return end;
    }


    public void checkHasPositions() {
        if (start.hasPosition()) {
            // OK
        } else {
            E.error("start point of " + id + " still has no position " + start.writePos());
        }
        if (end.hasPosition()) {
            // OK
        } else {
            E.error("start point of " + id + " still has no position " + start.writePos());
        }
    }


}
