package org.textensor.stochdiff.model;

import org.textensor.report.E;


public class Specie {

    public String name;
    public String id;

    public double kdiff;
    public String kdiffunit;

    private int index;


    public Specie() {
        kdiff = Double.NaN;
        kdiffunit = null;
    }


    public String getID() {
        return id;
    }

    public void setIndex(int ict) {
        index = ict;
    }

    public int getIndex() {
        return index;
    }



    public double getDiffusionConstant() {
        double ret = 0.;
        if (!(Double.isNaN(kdiff))) {
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
        double ret = 0.;
        // output units are microns^2/ms


        if (su.equals("mu2/s")) {
            ret = 0.001;

        } else if (su.equals("m2/s")) {
            ret = 1.e9;

        } else if (su.equals("cm2/s")) {
            ret = 1.e5;

        } else if (su.equals("mu2/ms")) {
            ret = 1.;

        } else {
            E.error("don't understand units " + su);
        }
        return ret;
    }


}
