package org.textensor.stochdiff.model;

import org.textensor.report.E;


public class Specie {

    public String name = null;
    public String id = null;

    public double kdiff = Double.NaN;
    public String kdiffunit = null;

    private int index;

    public String getID() {
        return id != null ? id : generateID(name);
    }

    public static String generateID(String name) {
        return name.replaceAll(" /\\\\", "_");
    }

    public String getName() {
        return name;
    }

    public void setIndex(int ict) {
        index = ict;
    }

    public int getIndex() {
        return index;
    }



    public double getDiffusionConstant() {
        double ret = 0.;
        if (!Double.isNaN(kdiff)) {
            ret = kdiff;

            if (kdiffunit != null) {
                ret *= getFactor(kdiffunit);
            } else {
                ret *= getFactor("mu2/s");
            }
        }
        return ret;
    }


    private double getFactor(String su) {
        // output units are microns^2/ms

        if (su.equals("mu2/s"))
            return 0.001;

        if (su.equals("m2/s"))
            return 1.e9;

        if (su.equals("cm2/s"))
            return 1.e5;

        if (su.equals("mu2/ms"))
            return 1.;

        E.error("don't understand units " + su);
        throw new RuntimeException("don't understand units " + su);
    }
}
