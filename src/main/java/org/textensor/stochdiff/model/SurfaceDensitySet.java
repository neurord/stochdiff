package org.textensor.stochdiff.model;

import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.bind.annotation.*;

import org.textensor.stochdiff.inter.FloatValued;
import org.textensor.util.inst;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class SurfaceDensitySet implements Regional {
    static final Logger log = LogManager.getLogger();

    @XmlAttribute public String region;

    @XmlElement(name="PicoSD")
    public ArrayList<PicoSD> sds;

    transient HashMap<String, PicoSD> sdHM;

    public HashMap<String, PicoSD> getSurfaceDensityHM() {
        if (sdHM == null) {
            sdHM = inst.newHashMap();
            if (sds != null)
                for (PicoSD sd : sds)
                    sdHM.put(sd.specieID, sd);
        }
        return sdHM;
    }

    public double[] getPicoSurfaceDensities(String[] ids) {
        double[] ret = new double[ids.length];
        HashMap<String, PicoSD> chm = getSurfaceDensityHM();

        for (int i = 0; i < ids.length; i++)
            if (chm.containsKey(ids[i]))
                ret[i] = chm.get(ids[i]).getPicoMoleSurfaceDensity();
            else
                ret[i] = Double.NaN;

        log.debug("pico surface densities: {} → {}", ids, ret);
        return ret;
    }

    public boolean hasRegion() {
        return region != null;
    }

    public String getRegion() {
        return region;
    }

    public void addFloatValued(ArrayList<FloatValued> afv) {
        afv.addAll(sds);
    }
}
