package org.textensor.stochdiff.neuroml;

import java.util.HashMap;

import org.textensor.stochdiff.model.EndPoint;
import org.textensor.stochdiff.model.MorphPoint;
import org.textensor.stochdiff.model.Segment;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class segment {
    static final Logger log = LogManager.getLogger();

    public String name;
    public String id;
    public String cable;

    public MorphMLPoint proximal;
    public MorphMLPoint distal;

    public String parent;

    public MorphMLProperties properties; // sometimes find this in Neuron output

    public String getID() {
        return id;
    }

    public String getParentID() {
        return parent;
    }


    public String getName() {
        return name;
    }

    public MorphMLPoint getProximal() {
        return proximal;
    }

    public MorphMLPoint getDistal() {
        return distal;
    }


    public Segment getStochDiffSegment(HashMap<String, String> cableHM) {
        Segment ret = new Segment();
        ret.id = getID();
        if (cable != null) {
            if (cableHM.containsKey(cable))
                ret.region = cableHM.get(cable);
            else
                log.warn("segment refers to cable " + cable + " which can't be found");
        }

        ret.start = new EndPoint(proximal.getID(), proximal.getX(), proximal.getY(), proximal.getZ(), proximal.getR());
        ret.end = new EndPoint(distal.getID(), distal.getX(), distal.getY(), distal.getZ(), distal.getR());

        if (parent != null) {
            ret.start.on = parent;
            ret.start.at = "end";
            // TODO fractionAlong somewhere?
        }
        return ret;
    }

}
