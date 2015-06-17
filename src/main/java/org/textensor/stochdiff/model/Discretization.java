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

    Double spineDeltaX;

    private Double defaultMaxElementSide;

    private String elementShape;

    private Double maxAspectRatio;

    @XmlJavaTypeAdapter(DoubleListAdapter.class)
    List<Double> surfaceLayers;

    private List<MaxElementSide> maxElementSide;

    transient private HashMap<String, Double> maxSideHM;

    public synchronized HashMap<String, Double> getResolutionHM() {
        if (this.maxSideHM == null) {
            this.maxSideHM = inst.newHashMap();
            if (this.maxElementSide != null)
                for (MaxElementSide side: this.maxElementSide) {
                    Double old = this.maxSideHM.put(side.region, side.value);
                    if (old != null) {
                        log.error("Duplicate maxElementSide for region '{}'", side.region);
                        throw new RuntimeException("Duplicate maxElementSide");
                    }
                }
        }
        return this.maxSideHM;
    }

    public boolean curvedElements() {
        return "Curved".equals(this.elementShape);
    }

    public double getMaxAspectRatio() {
        if (this.maxAspectRatio == null)
            return 0;
        else
            return this.maxAspectRatio;
    }

    public double[] getSurfaceLayers() {
        return ArrayUtil.toPrimitiveArray(this.surfaceLayers);
    }

    public double getDefaultMaxElementSide() {
        if (this.defaultMaxElementSide == null)
            return 1;
        else
            return this.defaultMaxElementSide;
    }

    public static final Discretization SINGLE_VOXEL = new Discretization();
    static {
        SINGLE_VOXEL.defaultMaxElementSide = Double.POSITIVE_INFINITY;
    }
}
