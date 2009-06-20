package org.catacomb.dataview.gui;

import org.catacomb.dataview.formats.DataHandler;
import org.catacomb.druid.gui.base.DruDataDisplay;
import org.catacomb.druid.gui.edit.DruListPanel;
import org.catacomb.interlish.annotation.ControlPoint;
import org.catacomb.interlish.annotation.Editable;
import org.catacomb.interlish.annotation.IOPoint;
import org.catacomb.interlish.content.DoubleValue;
import org.catacomb.interlish.structure.Controller;
import org.catacomb.interlish.structure.FrameDisplay;
import org.catacomb.interlish.structure.Value;
import org.catacomb.interlish.structure.ValueWatcher;


public class Mesh2plusTimeController implements Controller, FrameDisplay, ValueWatcher {

    @IOPoint(xid="dataDisplay")
    public DruDataDisplay dataDisplay;

    @Editable(xid="cmin")
    public DoubleValue cmin;

    @Editable(xid="cmax")
    public DoubleValue cmax;

    @Editable(xid="zval")
    public DoubleValue zval;

    @IOPoint(xid="variables")
    public DruListPanel variableList;

    @ControlPoint(xid="frameController")
    public FramePlayerController frameController;


    private DataHandler dataHandler;



    public Mesh2plusTimeController() {
        cmin = new DoubleValue(0.);
        cmax = new DoubleValue(100.);
        zval = new DoubleValue(0.);

        cmin.addValueWatcher(this);
        cmax.addValueWatcher(this);
        zval.addValueWatcher(this);
    }


    public void setDataHandler(DataHandler dh) {
        dataHandler = dh;
        dataDisplay.attachGraphicsController(dh);
        variableList.setItems(dataHandler.getPlotNames());
        frameController.applyData(dataHandler.getFrameValues());
    }


    public void resetRange() {
        double ca = dataHandler.getMinValue();
        double cb = dataHandler.getMaxValue();
        cmin.reportableSetDouble(ca, this);
        cmax.reportableSetDouble(cb, this);
        exportColorRange();
    }



    public void attached() {
        frameController.setFrameDisplay(this);
    }



    public void showFrame(int ifr) {
        dataHandler.setFrame(ifr);
        dataDisplay.repaint();
    }


    public void setPlot(String s) {
        // E.info("setting plot " + s);
        dataHandler.setPlot(s);
        dataDisplay.repaint();
    }


    public void valueChangedBy(Value pv, Object src) {
        exportColorRange();
    }


    public void exportColorRange() {
        double clo = cmin.getDouble();
        double chi = cmax.getDouble();
        dataHandler.setZValue(zval.getDouble());
        //  E.info("exporting color range " + clo + " " + chi);
        dataDisplay.setColorRange(clo, chi);
        dataDisplay.repaint();
    }


    public void repaint() {
        dataDisplay.repaint();

    }

}
