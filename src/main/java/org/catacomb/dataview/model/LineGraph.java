package org.catacomb.dataview.model;

import org.catacomb.interlish.structure.AddableTo;
import org.catacomb.report.E;

import java.util.ArrayList;


public class LineGraph implements AddableTo {

    public int width;
    public int height;


    public XAxis xaxis;
    public YAxis yaxis;

    public ArrayList<Line> lines = new ArrayList<Line>();

    public ArrayList<LineSet> lineSets = new ArrayList<LineSet>();

    public ArrayList<View> views = new ArrayList<View>();


    public void add(Object obj) {
        if (obj instanceof XAxis) {
            xaxis = (XAxis)obj;
        } else if (obj instanceof YAxis) {
            yaxis = (YAxis)obj;
        } else if (obj instanceof Line) {
            lines.add((Line)obj);
        } else if (obj instanceof LineSet) {
            lineSets.add((LineSet)obj);
        } else if (obj instanceof View) {
            views.add((View)obj);

        } else {
            E.error("cant add " + obj);
        }
    }



    public XAxis getXAxis() {
        return xaxis;
    }

    public YAxis getYAxis() {
        return yaxis;
    }


    public ArrayList<View> getViews() {
        return views;
    }


    public ArrayList<Plottable> getPlottables() {
        ArrayList<Plottable> ret = new ArrayList<Plottable>();
        ret.addAll(lines);
        ret.addAll(lineSets);
        return ret;
    }



    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }




}
