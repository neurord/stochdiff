package org.textensor.stochdiff.model;

import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.bind.annotation.*;

import org.textensor.stochdiff.inter.FloatValued;
import org.textensor.util.inst;

public class SurfaceDensitySet implements Regional {

    @XmlAttribute public String region;

    @XmlElement(name="SurfaceDensity")
    public ArrayList<SurfaceDensity> sds;

    transient HashMap<String, SurfaceDensity> sdHM;

    public HashMap<String, SurfaceDensity> getSurfaceDensityHM() {
        if (sdHM == null) {
            sdHM = inst.newHashMap();
            if (sds != null)
                for (SurfaceDensity sd : sds)
                    sdHM.put(sd.specieID, sd);
        }
        return sdHM;
    }

    public double[] getPicoSurfaceDensities(String[] ids) {
        double[] ret = new double[ids.length];
        HashMap<String, SurfaceDensity> chm = getSurfaceDensityHM();

        for (int i = 0; i < ids.length; i++)
            if (chm.containsKey(ids[i]))
                ret[i] = chm.get(ids[i]).getPicoMoleSurfaceDensity();
            else
                ret[i] = Double.NaN;

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
