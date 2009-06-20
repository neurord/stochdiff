package org.catacomb.dataview.gui;

import org.catacomb.dataview.formats.DataHandler;
import org.catacomb.druid.gui.base.DruDataDisplay;
import org.catacomb.interlish.annotation.IOPoint;
import org.catacomb.interlish.structure.Controller;
import org.catacomb.report.E;


public class BasicPlotController implements Controller {

    @IOPoint(xid="dataDisplay")
    public DruDataDisplay dataDisplay;




    public BasicPlotController() {

    }



    public void setDataHandler(DataHandler dh) {
        E.info("bpc set handler " + dh);
        dataDisplay.attachGraphicsController(dh);
    }




    public void attached() {
        // TODO Auto-generated method stub

    }



    public void repaint() {
        dataDisplay.repaint();

    }

}
