package org.textensor.stochdiff.model;

import java.util.ArrayList;
import java.util.HashMap;

import org.textensor.report.E;
import org.textensor.stochdiff.inter.AddableTo;

public class SurfaceDensitySet implements AddableTo {

    public String region;

    public ArrayList<SurfaceDensity> sds;

    HashMap<String, SurfaceDensity> sdHM;

    public boolean complete;



    public void add(Object obj) {
        if (sds == null) {
            sds = new ArrayList<SurfaceDensity>();
        }
        if (obj instanceof SurfaceDensity) {
            sds.add((SurfaceDensity)obj);
        } else {
            E.error("cant add " + obj);
        }
    }



    public HashMap<String, SurfaceDensity> getSurfaceDensityHM() {
        if (sdHM == null) {
            sdHM = new HashMap<String, SurfaceDensity>();
            if (sds != null) {
                for (SurfaceDensity sd : sds) {
                    sdHM.put(sd.specieID, sd);
                }
            }
        }
        return sdHM;
    }


    public double[] getPicoSurfaceDensities(String[] ids) {
        double[] ret = new double[ids.length];
        HashMap<String, SurfaceDensity> chm = getSurfaceDensityHM();

        for (int i = 0; i < ids.length; i++) {
            if (chm.containsKey(ids[i])) {
                ret[i] = chm.get(ids[i]).getPicoMoleSurfaceDensity();
            }
        }

        return ret;
    }


    public boolean hasRegion() {
        return (region != null);
    }

    public String getRegion() {
        return region;
    }

}
