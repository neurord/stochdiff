package org.catacomb.dataview.gui;

import org.catacomb.dataview.display.ViewConfig;
import org.catacomb.dataview.formats.DataHandler;
import org.catacomb.druid.gui.base.DruDataDisplay;
import org.catacomb.druid.gui.edit.DruListPanel;
import org.catacomb.interlish.annotation.IOPoint;
import org.catacomb.interlish.structure.Controller;
import org.catacomb.report.E;

import java.awt.image.BufferedImage;

public class DViewPlotController implements Controller {

    @IOPoint(xid="dataDisplay")
    public DruDataDisplay dataDisplay;

    @IOPoint(xid="plots")
    public DruListPanel plotsList;

    private DataHandler dataHandler;



    public DViewPlotController() {

    }



    public void setDataHandler(DataHandler dh) {
        dataHandler = dh;
        dataDisplay.attachGraphicsController(dh);

        dataDisplay.setXAxisLabel(dh.getXAxisLabel());
        dataDisplay.setYAxisLabel(dh.getYAxisLabel());


        plotsList.setItems(dh.getPlotNames());
        repaint();
    }


    public void showPlot(String s) {
        dataHandler.setPlot(s);
        ViewConfig vc = dataHandler.getViewConfig(s);
        if (vc == null) {
            E.warning("no such view " + vc);
        } else {
            dataDisplay.setLimits(vc.getLimits());
        }
    }


    public void attached() {
        repaint();

    }



    public void repaint() {
        dataDisplay.repaint();

    }



    public void setViewSize(int w, int h) {
        dataDisplay.setSize(w, h);

    }



    public BufferedImage getSnapshot() {
        return dataDisplay.getSnapshot();
    }

}
