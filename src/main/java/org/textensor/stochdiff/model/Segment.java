package org.textensor.stochdiff.model;

import java.util.HashMap;

import javax.xml.bind.annotation.*;

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
        this.start.setSegment(this);
        this.end.setSegment(this);

        this.start.resolve(segmentHM, this.end);
        this.end.resolve(segmentHM, this.start);

        this.start.addNeighbor(this.end);
        this.end.addNeighbor(this.start);

        if (this.region != null) {
            this.start.setRegionWith(this.end, this.region);
            this.end.setRegionWith(this.start, this.region);
        }

        if (this.id != null) {
            this.start.setIDWith(this.end, this.id);
            this.end.setIDWith(this.start, this.id);
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
        if (!start.hasPosition())
            throw new RuntimeException("Start point of " + id + " still has no position " + start.writePos());

        if (!end.hasPosition())
            throw new RuntimeException("Start point of " + id + " still has no position " + end.writePos());
    }
}
