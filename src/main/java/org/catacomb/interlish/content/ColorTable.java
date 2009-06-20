package org.catacomb.interlish.content;


import java.awt.Color;


public class ColorTable {


    String name;

    int nColor;

    Color[] colors;

    double rangeMin;
    double rangeMax;
    double rangeDelta;


    public ColorTable(String snm) {
        name = snm;
        nColor = 100;
        colors = new Color[nColor];
        Color c = Color.gray;
        for (int i = 0; i < nColor; i++) {
            colors[i] = c;
        }
        rangeMin = 0.;
        rangeMax = 1.;
        rangeDelta = 1.;
    }

    public static ColorTable makeDefaultColorTable() {
        return new ColorTable("default");
    }


    public String toString() {
        return name;
    }

    public String getName() {
        return name;
    }

    public int getNColor() {
        return nColor;
    }

    public Color getColor(int i) {
        return colors[i];
    }

    public void setNColor(int nc) {
        Color[] ac = new Color[nc];
        for (int i = 0; i < nc; i++) {
            ac[i] = interpColorAt((1. * i) / (nc - 1.));
        }

        colors = ac;
        nColor = nc;
    }


    public void setColor(int ic, Color c) {
        colors[ic] = c;
    }


    public Color[] getColors() {
        return colors;
    }



    public Color getColor(double f) {
        return getFractionalColor((f - rangeMin) / rangeDelta);
    }


    public Color getFractionalColor(double f) {
        double fr = f * nColor;
        int ic = (int)(fr + 0.5);
        if (ic < 0) {
            ic = 0;
        }
        if (ic >= nColor) {
            ic = nColor - 1;
        }
        return colors[ic];
    }



    public Color interpColorAt(double fracin) {
        double frac = fracin;
        if (frac < 0.) {
            frac = 0.;
        }
        if (frac > 1.) {
            frac = 1.;
        }
        double fr = frac * (nColor-1);
        int ipr = (int)fr;
        double f = fr - ipr;
        int inx = ipr+1;

        if (inx >= nColor) {
            inx = nColor - 1;
        }


        Color ca = colors[ipr];
        Color cb = colors[inx];

        int car = ca.getRed();
        int cag = ca.getGreen();
        int cab = ca.getBlue();

        int dr = cb.getRed() - car;
        int dg = cb.getGreen() - cag;
        int db = cb.getBlue() - cab;

        Color ret = new Color((int)(car + f * dr), (int)(cag + f * dg), (int)(cab + f * db));
        return ret;
    }






    public void interp(int a, int b) {
        Color ca = getColor(a);
        Color cb = getColor(b);

        int car = ca.getRed();
        int cag = ca.getGreen();
        int cab = ca.getBlue();

        int dr = cb.getRed() - car;
        int dg = cb.getGreen() - cag;
        int db = cb.getBlue() - cab;

        for (int i = a; i <= b; i++) {
            double f = (i - a) / (1. * (b - a));
            setColor(i, new Color((int)(car + f * dr), (int)(cag + f * dg), (int)(cab + f * db)));
        }
    }

    public void setRange(double dmin, double dmax) {
        rangeMin = dmin;
        rangeMax = dmax;
        syncRangeDelta();
    }

    private void syncRangeDelta() {
        rangeDelta = rangeMax - rangeMin;
        if (rangeDelta == 0) {
            rangeDelta = 1.;
        }
    }

    public void setRangeMin(double d) {
        rangeMin = d;
        syncRangeDelta();
    }

    public void setRangeMax(double d) {
        rangeMax = d;
        syncRangeDelta();
    }


    public double getRangeMin() {
        return rangeMin;
    }


    public double getRangeMax() {
        return rangeMax;
    }


}
