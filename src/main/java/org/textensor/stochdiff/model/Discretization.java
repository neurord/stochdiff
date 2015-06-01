package org.textensor.stochdiff.model;

import java.util.HashMap;
import java.util.List;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.*;

import org.textensor.util.inst;
import org.textensor.util.ArrayUtil;
import org.textensor.xml.DoubleListAdapter;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class Discretization {
    static final Logger log = LogManager.getLogger(Discretization.class);

    public double spineDeltaX;

    public double defaultMaxElementSide = 1.;

    public String elementShape = "Cuboid";

    public double maxAspectRatio= 0;

    @XmlElement(name="surfaceLayers")
    @XmlJavaTypeAdapter(DoubleListAdapter.class)
    List<Double> surfaceLayers;

    @XmlElement(name="MaxElementSide")
    public List<MaxElementSide> sides = inst.newArrayList();

    transient private HashMap<String, Double> maxSideHM;

    public synchronized HashMap<String, Double> getResolutionHM() {
        if (this.maxSideHM == null) {
            this.maxSideHM = inst.newHashMap();
            for (MaxElementSide side: this.sides) {
                Double old = this.maxSideHM.put(side.region, side.value);
                if (old != null) {
                    log.error("Duplicate MaxElementSide for region '{}'", side.region);
                    throw new RuntimeException("Duplicate MaxElementSide");
                }
            }
        }
        return this.maxSideHM;
    }

    public boolean curvedElements() {
        return elementShape.equals("Curved");
    }

    public double getMaxAspectRatio() {
        return maxAspectRatio;
    }

    public double[] getSurfaceLayers() {
        return ArrayUtil.toPrimitiveArray(this.surfaceLayers);
    }

    public static final Discretization SINGLE_VOXEL = new Discretization();
    static {
        SINGLE_VOXEL.defaultMaxElementSide = Double.POSITIVE_INFINITY;
    }
}
