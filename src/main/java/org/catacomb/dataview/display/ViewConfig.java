package org.catacomb.dataview.display;


public class ViewConfig {

    String id;
    double[] xyxy;


    public ViewConfig(String s, double[] da) {
        id = s;
        xyxy = da;
    }


    public String getID() {
        return id;
    }


    public double[] getLimits() {
        return xyxy;
    }


}
