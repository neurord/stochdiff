package org.textensor.stochdiff.model;


import java.util.HashMap;

import org.textensor.report.E;
import org.textensor.stochdiff.inter.AddableTo;


public class Discretization implements AddableTo {

    public double spineDeltaX;


    public double defaultMaxElementSide = 1.;


    public String elementShape = "Cuboid";

    public double surfaceLayer = 0;
    public double maxAspectRatio= 0;

//    public double[] surfaceLayers = null;



    public SurfaceLayers surfaceLayers;


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

            /*
            else if (obj instanceof MaxAspectRatio) {
            	maxAspectRatio = ((MaxAspectRatio)obj).value;

            } else if (obj instanceof SurfaceLayers) {
            	surfaceLayers = ((SurfaceLayers)obj).getValues();
            	*/
        } else {
            E.warning("unrecognized object " + obj);
        }
    }


    public HashMap<String, Double> getResolutionHM() {
        return maxSideHM;
    }

    public boolean curvedElements() {
        boolean ret = false;
        String eslc = elementShape.toLowerCase();
        if (eslc.equals("curved")) {
            ret = true;
        } else if (eslc.equals("cuboid")) {
            ret = false;
        } else {
            E.error("unrecognized element shape (need 'curved' or 'cuboid'): " + elementShape);
        }
        return ret;
    }


    public double getMaxAspectRatio() {
        return maxAspectRatio;
    }

    public double[] getSurfaceLayers() {
        double[] ret = new double[0];
        if (surfaceLayers != null) {
            ret = surfaceLayers.getValues();
        } else if (surfaceLayer > 0) {
            ret = new double[1];
            ret[0] = surfaceLayer;
        }
        return ret;
    }


}
