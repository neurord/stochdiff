package org.textensor.stochdiff.model;


public class SpecieRef {

    public String specieID;

    public int n = 1;

    public int power = 1;

    public int getStochiometry() {
        return n;
    }

    public int getPower() {
        return power;
    }

    public String getSpecieID() {
        return specieID;
    }
}
