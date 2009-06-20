package org.catacomb.dataview.formats;

import java.io.File;

import org.catacomb.datalish.Box;
import org.catacomb.dataview.display.ViewConfig;
import org.catacomb.graph.gui.Painter;
import org.catacomb.report.E;
import org.catacomb.util.ArrayUtil;


public class MeshSummary implements DataHandler {

    Mesh2plusTimeDisplay meshDH;


    double[] xdat;

    double[][] ydat;

    double ymin;
    double ymax;

    public MeshSummary(DataHandler dh) {
        meshDH = (Mesh2plusTimeDisplay)dh;
        xdat = meshDH.getFrameValues();
        String[] sa = dh.getPlotNames();

        E.info("setting plot " + sa[sa.length-1]);

        setPlot(sa[sa.length-1]);
    }


    public int getContentStyle() {
        return DataHandler.STATIC;
    }


    public double[] getFrameValues() {
        return null;
    }


    public String getMagic() {
        return null;
    }


    public double getMaxValue() {
        return 0;
    }


    public double getMinValue() {
        // TODO Auto-generated method stub
        return 0;
    }


    public String[] getPlotNames() {
        return meshDH.getPlotNames();
    }


    public String[] getViewOptions() {
        // TODO Auto-generated method stub
        return null;
    }


    public void read(File f) {
        // TODO Auto-generated method stub

    }


    public void setFrame(int ifr) {
        // TODO Auto-generated method stub

    }


    public void setPlot(String s) {
        ydat = new double[5][xdat.length];
        for (int i = 0; i < xdat.length; i++) {

            double[] pdat = meshDH.getFrameData(s, i);
            ydat[0][i] = ArrayUtil.minD(pdat);
            ydat[1][i] = ArrayUtil.maxD(pdat);
            ydat[2][i] = ArrayUtil.avg(pdat);
            double sigma = ArrayUtil.sd(pdat);
            ydat[3][i] = ydat[2][i] - sigma;
            ydat[4][i] = ydat[2][i] + sigma;
        }
        ymin = ArrayUtil.min(ydat);
        ymax = ArrayUtil.max(ydat);

    }


    public void setViewStyle(String s) {
        // TODO Auto-generated method stub

    }


    public boolean antialias() {
        // TODO Auto-generated method stub
        return false;
    }


    public Box getLimitBox() {
        Box b = new Box(xdat[0], ymin, xdat[xdat.length-1], ymax);
        return b;
    }


    public void instruct(Painter p) {
        if (ydat != null) {
            int nx = xdat.length;
            p.setColorRed();
            p.drawPolyline(xdat, ydat[0], nx);
            p.drawPolyline(xdat, ydat[1], nx);
            p.setColorWhite();
            p.drawPolyline(xdat, ydat[2], nx);
            p.setColorBlue();
            p.drawPolyline(xdat, ydat[3], nx);
            p.drawPolyline(xdat, ydat[4], nx);
        }

    }


    public DataHandler getCoHandler() {
        return null;
    }


    public boolean hasData() {
        return true;
    }


    public String getXAxisLabel() {
        // TODO Auto-generated method stub
        return null;
    }


    public String getYAxisLabel() {
        // TODO Auto-generated method stub
        return null;
    }


    public ViewConfig getViewConfig(String s) {
        // TODO Auto-generated method stub
        return null;
    }


    public void setZValue(double d) {
        // TODO Auto-generated method stub

    }

}
