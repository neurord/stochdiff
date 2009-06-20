package org.catacomb.dataview.model;


public class View {

    public String id;
    public double xmin;
    public double xmax;
    public double ymin;
    public double ymax;



    public String getID() {
        return id;
    }

    public double[] getXYXY() {
        double[] ret = {xmin, ymin, xmax, ymax};
        return ret;
    }



}
