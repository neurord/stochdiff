package org.catacomb.interlish.content;


public class NVPair {

    String name;
    double value;
    String sValue;


    public NVPair(String sn, double v, String sv) {
        name = sn;
        value = v;
        sValue = sv;
    }





    public String toString() {
        return name;
    }

    public String getName() {
        return name;
    }

    public double getValue() {
        return value;
    }

    public String getSValue() {
        return sValue;
    }


    public boolean isCalled(String s) {
        return name.equals(s);
    }

    public void setValue(double d) {
        value = d;
    }

    public void setValue(double d, String sd) {
        value = d;
        sValue = sd;
    }

}
