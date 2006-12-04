package org.textensor.stochdiff.disc;

import java.util.HashMap;


import org.textensor.report.E;
import org.textensor.stochdiff.numeric.morph.TreePoint;


public class Resolution {

    double defaultDelta;

    HashMap<String, Double> deltaHM;


    public Resolution(double d, HashMap<String, Double> resHM) {
        defaultDelta = d;
        deltaHM = resHM;
    }


    public double getLocalDelta(TreePoint cpa, TreePoint cpb) {


        double localDelta = defaultDelta;
        if (deltaHM != null) {
            String id = cpa.segmentIDWith(cpb);
            String region = cpa.regionClassWith(cpb);


            if (id != null && deltaHM != null && deltaHM.containsKey(id)) {
                localDelta = deltaHM.get(id).doubleValue();

            } else if (region != null && deltaHM != null && deltaHM.containsKey(region)) {
                localDelta = deltaHM.get(region).doubleValue();
            }
        }


        return localDelta;
    }



}
