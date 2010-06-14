package org.textensor.stochdiff.neuroml;

public class MorphMLPoint {

    public String id;

    public double x;
    public double y;
    public double z;
    public double diameter;


    public String getID() {
        return id;
    }
    public double getR() {
        return diameter / 2.;
    }
    public double getX() {
        return x;
    }
    public double getY() {
        return y;
    }
    public double getZ() {
        return z;
    }

}
