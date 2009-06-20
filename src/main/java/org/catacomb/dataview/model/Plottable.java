package org.catacomb.dataview.model;


import java.util.ArrayList;

import org.catacomb.datalish.SColor;
import org.catacomb.dataview.display.Displayable;
import org.catacomb.numeric.data.AsciiIO;
import org.catacomb.report.E;


import java.awt.Color;

import java.io.File;

public abstract class Plottable {

    public String file;
    public SColor color;
    public double width;
    public String rescale;
    public String label;
    public String function;

    public abstract ArrayList<Displayable> getDisplayables(File fdir);



    public Color getColor() {
        Color ret = Color.white;
        if (color != null) {
            ret = color.getColor();
        }
        return ret;
    }



    public double[] getScaleFactors(int nsf) {
        double[] ret = new double[nsf];
        for (int i = 0; i < nsf; i++) {
            ret[i] = 1.;
        }
        if (rescale == null) {
            // leave as is;

        } else {
            double[] row = AsciiIO.readRow(rescale);
            if (row == null) {
                E.warning("cant read scale factors from " + rescale);
            } else {
                for (int i = 0; i < nsf && i < row.length; i++) {
                    ret[i] = row[i];
                }

            }
        }
        return ret;
    }


    protected double[] scaleColumn(double[] d, double sf) {
        double[] ret = new double[d.length];
        for (int i = 0; i < d.length; i++) {
            ret[i] = sf * d[i];
        }
        return ret;
    }

}
