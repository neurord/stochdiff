package org.catacomb.dataview.gui;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import org.catacomb.dataview.display.ViewConfig;
import org.catacomb.dataview.formats.DataHandler;
import org.catacomb.dataview.formats.PolyLineHandler;
import org.catacomb.dataview.model.LineGraph;
import org.catacomb.dataview.model.Plottable;
import org.catacomb.dataview.model.View;
import org.catacomb.druid.dialog.Dialoguer;
import org.catacomb.druid.gui.edit.DruMenu;
import org.catacomb.druid.load.DruidResourceLoader;
import org.catacomb.interlish.annotation.ControlPoint;
import org.catacomb.interlish.annotation.IOPoint;
import org.catacomb.interlish.reflect.ReflectionConstructor;
import org.catacomb.interlish.service.ResourceAccess;
import org.catacomb.interlish.structure.Controller;
import org.catacomb.report.E;
import org.catacomb.serial.Deserializer;
import org.catacomb.util.FileUtil;

public class DViewController implements Controller {


    @IOPoint(xid="ViewMenu")
    public DruMenu viewMenu;

    @ControlPoint(xid="plotController")
    public DViewPlotController basicController;


    private DataHandler dataHandler;

    ArrayList<ViewConfig> viewConfigs;

    public DViewController() {

    }


    public void open() {
        File f = Dialoguer.getFile("CCViz");
        if (f != null) {
            open(f);
        }
    }

    public void open(File fin) {
        File f = fin;
        if (f.isDirectory()) {
            f = findConfig(f);
        }


        DruidResourceLoader drl = new DruidResourceLoader();
        drl.addPath("org.catacomb.dataview.model");
        ResourceAccess.setResourceLoader(drl);

        ReflectionConstructor.addPath("org.catacomb.dataview.model");

        String s = FileUtil.readStringFromFile(f);
        Object obj = Deserializer.deserialize(s);

        if (obj instanceof LineGraph) {
            LineGraph lineGraph = (LineGraph)obj;

            PolyLineHandler plh = new PolyLineHandler();
            dataHandler = plh;

            for (Plottable pl : lineGraph.getPlottables()) {
                plh.addItems(pl.getDisplayables(f.getParentFile()));
            }

            plh.setXAxis(lineGraph.getXAxis());
            plh.setYAxis(lineGraph.getYAxis());

            viewConfigs = new ArrayList<ViewConfig>();
            for (View v : lineGraph.getViews()) {
                ViewConfig vc = new ViewConfig(v.getID(), v.getXYXY());
                viewConfigs.add(vc);
                plh.addView(vc);
            }

            int w = lineGraph.getWidth();
            int h = lineGraph.getHeight();
            if (w > 99 && h > 99) {
                basicController.setViewSize(w, h);
            }


            basicController.setDataHandler(plh);
        }






        /*
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

              } else {
                 E.missing();
              }

           }

           */
    }

    private File findConfig(File fdir) {
        File ret = null;
        for (File f : fdir.listFiles()) {
            String s = FileUtil.readFirstLine(f);
            s = s.trim();
            if (s.startsWith("<") && s.indexOf("LineGraph") > 0) {
                ret = f;
                break;
            }
        }
        return ret;
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
    }


    public void attached() {
        // TODO Auto-generated method stub

    }


    public void makeImages(File fdir) {
        for (ViewConfig vc : viewConfigs) {

            basicController.showPlot(vc.getID());

            File fout = new File(fdir, vc.getID().replace(" ", "_") + ".png");
            try {
                Thread.sleep(1000);
            } catch (Exception ex) {

            }
            BufferedImage bim = basicController.getSnapshot();
            try {
                ImageIO.write(bim, "png", fout);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

    }

}
