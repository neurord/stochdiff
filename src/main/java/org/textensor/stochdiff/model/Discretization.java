package org.textensor.stochdiff.model;

import java.util.HashMap;
import java.util.List;

import javax.xml.bind.annotation.*;

import org.textensor.util.inst;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class Discretization {
    static final Logger log = LogManager.getLogger(Discretization.class);

    public double spineDeltaX;


    public double defaultMaxElementSide = 1.;


    public String elementShape = "Cuboid";

    public double surfaceLayer = 0;
    public double maxAspectRatio= 0;

    public SurfaceLayers surfaceLayers;

    @XmlElement(name="MaxElementSide")
    public List<MaxElementSide> sides;

    private HashMap<String, Double> maxSideHM;

    public synchronized HashMap<String, Double> getResolutionHM() {
        if (this.maxSideHM == null) {
            this.maxSideHM = inst.newHashMap();
            for(MaxElementSide side: this.sides) {
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
        if (surfaceLayers != null)
            return surfaceLayers.getValues();
        else if (surfaceLayer > 0)
            return new double[]{ surfaceLayer };
        else
            return new double[]{ };
    }
}
