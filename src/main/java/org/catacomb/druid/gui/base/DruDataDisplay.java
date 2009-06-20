package org.catacomb.druid.gui.base;

import java.awt.Color;
import java.awt.image.BufferedImage;

import org.catacomb.druid.swing.DBorderLayout;
import org.catacomb.graph.gui.DataDisplay;
import org.catacomb.interlish.structure.GraphicsView;
import org.catacomb.interlish.structure.ModeController;
import org.catacomb.interlish.structure.RangeWatcher;
import org.catacomb.util.AWTUtil;

public class DruDataDisplay extends DruBorderPanel implements GraphicsView {
    static final long serialVersionUID = 1001;

    DataDisplay dataDisplay;



    public DruDataDisplay(int w, int h) {

        dataDisplay = new DataDisplay(w, h);

        addDComponent(dataDisplay, DBorderLayout.CENTER);
    }


    public void setBg(Color c) {
        super.setBg(c);
        dataDisplay.setBg(c);
    }


    public void setModeController(ModeController mc) {
        mc.addModeSettable(dataDisplay);
    }


    public void viewChanged() {
        dataDisplay.viewChanged();
    }



    public void attachGraphicsController(Object obj) {
        dataDisplay.attach(obj);
    }


    public void setXAxisLabel(String s) {
        dataDisplay.setXAxisLabel(s);
    }

    public void setXAxis(String s, double xl, double xh) {
        dataDisplay.setXAxis(s, xl, xh);
    }

    public void setYAxisLabel(String s) {
        dataDisplay.setYAxisLabel(s);
    }

    public void setYAxis(String s, double yl, double yh) {
        dataDisplay.setYAxis(s, yl, yh);
    }


    public void setXRange(double low, double high) {
        dataDisplay.setXRange(low, high);
    }

    public void setYRange(double low, double high) {
        dataDisplay.setYRange(low, high);
    }

    public void setLimits(double[] xyxy) {
        dataDisplay.setLimits(xyxy);
    }


    public double[] getXRange() {
        return dataDisplay.getXRange();
    }

    public double[] getYRange() {
        return dataDisplay.getYRange();
    }

    public void setFixedAspectRatio(double ar) {
        dataDisplay.setFixedAspectRatio(ar);
    }

    public void reframe() {
        dataDisplay.reframe();
    }


    public void setColorRange(double cmin, double cmax) {
        dataDisplay.setColorRange(cmin, cmax);
    }

    public void setColorTable(Color[] ac) {
        dataDisplay.setColorTable(ac);
    }


    public void addRangeWatcher(RangeWatcher rw) {
        dataDisplay.addRangeWatcher(rw);
    }


    public void setSize(int w, int h) {
        dataDisplay.setPrefSize(w, h);
        setPreferredSize(w, h);
        revalidate();

    }


    public void repaintAll() {
        dataDisplay.repaintAll();
    }

    public void syncSizes() {
        dataDisplay.syncSizes();
    }

    public BufferedImage getSnapshot() {
        // BufferedImage img = AWTUtil.getBufferedImage(getGUIPeer());
        BufferedImage img = AWTUtil.getBufferedImage(dataDisplay);
        return img;
    }


    /*
    public void setXRange(double xmin, double xmax) {
       dataDisplay.setXRange(xmin, xmax);
    }


    public void setYRange(double ymin, double ymax) {
       dataDisplay.setYRange(ymin, ymax);
    }
    */

}
