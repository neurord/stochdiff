package org.textensor.stochdiff.model;


import java.util.HashMap;

import org.textensor.report.E;
import org.textensor.stochdiff.inter.AddableTo;


public class Discretization implements AddableTo {

    public double spineDeltaX;


    public double defaultMaxElementSide;

    public HashMap<String, Double> maxSideHM;




    public void add(Object obj) {
        if (obj instanceof MaxElementSide) {
            MaxElementSide mes = (MaxElementSide)obj;
            if (mes.region != null) {
                String reg = mes.region.trim();
                if (reg.length() > 0) {
                    if (maxSideHM == null) {
                        maxSideHM = new HashMap<String, Double>();
                    }
                    maxSideHM.put(reg, new Double(mes.value));

                } else {
                    if (defaultMaxElementSide <= 0) {
                        defaultMaxElementSide = mes.value;
                    }
                }
            }

        } else {
            E.warning("unrecognized object " + obj);
        }
    }


    public HashMap<String, Double> getResolutionHM() {
        return maxSideHM;
    }

}
