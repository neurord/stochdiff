package org.catacomb.dataview.gui;

import java.io.File;

import org.catacomb.dataview.formats.DataHandler;
import org.catacomb.dataview.formats.DataHandlerFactory;
import org.catacomb.dataview.formats.MeshSummary;
import org.catacomb.druid.dialog.Dialoguer;
import org.catacomb.druid.gui.edit.DruMenu;
import org.catacomb.interlish.annotation.ControlPoint;
import org.catacomb.interlish.annotation.IOPoint;
import org.catacomb.interlish.structure.Controller;
import org.catacomb.report.E;
import org.catacomb.util.FileUtil;


public class CCVizController implements Controller {


    @IOPoint(xid="ViewMenu")
    public DruMenu viewMenu;

    @ControlPoint(xid="basicController")
    public BasicPlotController basicController;

    @ControlPoint(xid="mesh2Controller")
    public Mesh2plusTimeController mesh2Controller;



    private DataHandler dataHandler;


    public CCVizController() {

    }


    public void open() {
        File f = Dialoguer.getFile("CCViz");
        if (f != null) {
            open(f);
        }
    }

    public void open(File f) {
        String s = FileUtil.readFirstLine(f);

        dataHandler = DataHandlerFactory.getHandler(s);
        if (dataHandler == null) {
            int[] xy = {500, 500};
            Dialoguer.message(xy, "unrecognized file - cannot plot " + s);
        } else {
            dataHandler.read(f);

            syncOptions();

            int ic = dataHandler.getContentStyle();
            if (ic == DataHandler.STATIC) {
                basicController.setDataHandler(dataHandler);

            } else if (ic == DataHandler.FRAMES2D) {
                mesh2Controller.setDataHandler(dataHandler);

                DataHandler dh = dataHandler.getCoHandler();
                if (dh != null && dh.getContentStyle() == DataHandler.STATIC && dh.hasData()) {
                    basicController.setDataHandler(dh);

                } else {
                    basicController.setDataHandler(new MeshSummary(dataHandler));
                }

            } else {
                E.missing();
            }

        }
    }

    public void requestClose() {
        exit();
    }


    public void reload() {
        E.info("time to reload...");
    }


    public void requestExit() {
        exit();
    }


    public void exit() {
        System.exit(3);
    }


    public void syncOptions() {
        String[] sa = new String[0];
        if (dataHandler != null) {
            sa = dataHandler.getViewOptions();
        }
        viewMenu.setOptions(sa);
    }


    public void setViewStyle(String s) {

        if (dataHandler != null) {
            dataHandler.setViewStyle(s);
        }

        basicController.repaint();
        mesh2Controller.repaint();
    }


    public void attached() {
        // TODO Auto-generated method stub

    }

}
