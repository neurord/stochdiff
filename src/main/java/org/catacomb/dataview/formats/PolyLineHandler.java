package org.catacomb.dataview.formats;

import java.io.File;
import java.util.ArrayList;

import org.catacomb.datalish.Box;
import org.catacomb.dataview.display.Displayable;
import org.catacomb.dataview.display.ViewConfig;
import org.catacomb.dataview.model.XAxis;
import org.catacomb.dataview.model.YAxis;
import org.catacomb.graph.gui.Painter;

import java.util.HashMap;

public class PolyLineHandler implements DataHandler {


    ArrayList<ViewConfig> views = new ArrayList<ViewConfig>();
    HashMap<String, ViewConfig> viewHM = new HashMap<String, ViewConfig>();

    String[] plotNames = null;
    String[] viewOptions = {"lines"};


    XAxis xaxis;
    YAxis yaxis;


    ArrayList<Displayable> displayables;



    public DataHandler getCoHandler() {
        return null;
    }

    public int getContentStyle() {
        return DataHandler.STATIC;
    }

    public double[] getFrameValues() {
        return null;
    }

    public String getMagic() {
        return "cctable";
    }



    public double getMaxValue() {
        // TODO Auto-generated method stub
        return 0;
    }

    public double getMinValue() {
        // TODO Auto-generated method stub
        return 0;
    }


    public String[] getPlotNames() {
        if (plotNames == null || plotNames.length != views.size()) {
            int npn = views.size();
            plotNames = new String[npn];
            for (int i = 0; i < npn; i++) {
                plotNames[i] = views.get(i).getID();
            }
        }
        return plotNames;
    }


    public String[] getViewOptions() {
        return viewOptions;
    }

    public boolean hasData() {
        return (displayables != null && displayables.size() > 0);
    }

    public void read(File f) {

    }

    public void setFrame(int ifr) {

    }

    public void setPlot(String s) {

    }

    public void setViewStyle(String s) {

    }

    public boolean antialias() {
        // TODO Auto-generated method stub
        return false;
    }






    public Box getLimitBox() {
        Box b = new Box();
        if (displayables != null) {
            for (Displayable dbl : displayables) {
                dbl.pushBox(b);
            }
        }
        return b;
    }


    public Box getDefaultBox() {
        Box b = new Box();
        if (xaxis != null) {
            b.setXMin(xaxis.getMin());
            b.setXMax(xaxis.getMax());
        }
        if (yaxis != null) {
            b.setYMin(yaxis.getMin());
            b.setYMax(yaxis.getMax());
        }
        return b;
    }

    public void instruct(Painter p) {
        if (displayables != null) {
            for (Displayable dbl : displayables) {
                dbl.instruct(p);
            }
        }
    }

    public void setXAxis(XAxis ax) {
        xaxis = ax;

    }

    public void setYAxis(YAxis ax) {
        yaxis = ax;

    }


    public String getXAxisLabel() {
        String ret = "";
        if (xaxis != null) {
            ret = xaxis.getLabel();
        }
        return ret;
    }

    public String getYAxisLabel() {
        String ret = "";
        if (yaxis != null) {
            ret = yaxis.getLabel();
        }
        return ret;
    }




    public void addItems(ArrayList<Displayable> items) {
        if (displayables == null) {
            displayables = new ArrayList<Displayable>();
        }
        displayables.addAll(items);

    }

    public void addView(ViewConfig vc) {
        views.add(vc);
        viewHM.put(vc.getID(), vc);
    }

    public ViewConfig getViewConfig(String s) {
        ViewConfig ret = null;
        if (viewHM.containsKey(s)) {
            ret = viewHM.get(s);
        }
        return ret;
    }

    public void setZValue(double d) {
        // TODO Auto-generated method stub

    }

}
