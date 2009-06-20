package org.catacomb.dataview.formats;

import java.io.File;

import org.catacomb.dataview.display.ViewConfig;
import org.catacomb.graph.gui.PaintInstructor;

public interface DataHandler extends PaintInstructor {


    public String getMagic();

    public void read(File f);

    public String[] getViewOptions();

    public void setViewStyle(String s);

    public String[] getPlotNames();

    public double getMinValue();

    public double getMaxValue();

    public double[] getFrameValues();


    public final static int STATIC = 1;
    public final static int FRAMES2D = 2;

    public int getContentStyle();

    public void setFrame(int ifr);

    public void setPlot(String s);

    public DataHandler getCoHandler();

    public boolean hasData();

    public String getXAxisLabel();

    public String getYAxisLabel();

    public ViewConfig getViewConfig(String s);

    public void setZValue(double d);

}
