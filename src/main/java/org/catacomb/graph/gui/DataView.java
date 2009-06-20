package org.catacomb.graph.gui;

import org.catacomb.interlish.structure.IDable;
import org.catacomb.util.StringUtil;


import java.util.ArrayList;


// REFAC split for movies, evt sequences and x-y data;

public class DataView implements IDable {

    public String id;
    public String abscissa;
    public String[] ordinates;

    public String multiOrdinate;
    public int[] multiIndexes;

    public double xMin;
    public double xMax;
    public double yMin;
    public double yMax;


    public final static int XY = 1;
    public final static int MOVIE = 2;
    public final static int EVTSEQ = 3;
    public int style;


    public String itemPath;



    public DataView() {
        style = XY;
    }

    public DataView(String s) {
        id = s;
        style = XY;
    }


    public DataView makeCopy() {
        DataView dv = new DataView(id);
        dv.setAbscissa(abscissa);
        dv.setOrdinates(StringUtil.copyArray(ordinates));
        dv.setMultiOrdinate(multiOrdinate);
        dv.setMultiIndexes(multiIndexes);
        dv.setXRange(xMin, xMax);
        dv.setYRange(yMin, yMax);
        dv.itemPath = itemPath;
        dv.style = style;
        return dv;
    }



    public void setID(String s) {
        id = s;
    }


    public String getID() {
        return id;
    }

    public boolean isMovie() {
        return (style == MOVIE);
    }

    public boolean isXY() {
        return (style == XY);
    }

    public boolean isEventSequence() {
        return (style == EVTSEQ);
    }



    public void setMoviePath(String s) {
        style = MOVIE;
        itemPath = s;
    }

    public void setEventSequencePath(String s) {
        style = EVTSEQ;
        itemPath = s;
    }

    public String getItemPath() {
        return itemPath;
    }

    public String getMoviePath() {
        return itemPath;
    }

    public void setAbscissa(String a) {
        abscissa = a;
    }

    public String getAbscissa() {
        return abscissa;
    }

    public void setOrdinates(String[] sa) {
        ordinates = sa;
    }

    public void setOrdinates(ArrayList<String> als) {
        ordinates = als.toArray(new String[0]);
    }


    public String[] getOrdinates() {
        return ordinates;
    }


    public void setXRange(double[] da) {
        setXRange(da[0], da[1]);
    }

    public void setYRange(double[] da) {
        setYRange(da[0], da[1]);
    }

    public void setXRange(double d, double e) {
        xMin = d;
        xMax = e;
    }

    public void setYRange(double d, double e) {
        yMin = d;
        yMax = e;
    }

    public boolean hasLimits() {
        return (xMax > xMin && yMax > yMin);
    }

    public double[] getXYXYLimits() {
        double[] d = {xMin, yMin, xMax, yMax};
        return d;
    }


    public String getMultiOrdinate() {
        return multiOrdinate;
    }

    public void setMultiOrdinate(String s) {
        multiOrdinate = s;
    }

    public void clearMultiOrdinate() {
        multiOrdinate = null;
    }

    public void setMultiIndexes(int[] ia) {
        multiIndexes = ia;
    }

    public int[] getMultiIndexes() {
        return multiIndexes;
    }

    public void setAllMultiIndexes() {
        multiIndexes = null; // if not set, assume all;
    }

    public void setNoneMultiIndexes() {
        multiIndexes = new int[0];
    }
}
