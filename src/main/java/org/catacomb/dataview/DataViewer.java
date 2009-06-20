package org.catacomb.dataview;

import org.catacomb.dataview.build.Dataview;
import org.catacomb.dataview.read.ContentReader;
import org.catacomb.dataview.read.Importer;
import org.catacomb.druid.build.Context;
import org.catacomb.druid.gui.base.DruApplication;
import org.catacomb.interlish.reflect.ReflectionConstructor;


import java.io.File;


public class DataViewer {


    DataviewController controller;


    public DataViewer(File fconf) {

        ContentReader dsr = Importer.getReader(fconf);

        Object obj = dsr.getMain();


        Dataview dv = (Dataview)obj;

        Context ctxt = new Context();


        DruApplication druapp = dv.buildApplication(ctxt);

        druapp.pack();
        druapp.show();

        controller = new DataviewController(dv);
        controller.setMainFrame(druapp.getMainFrame());


        //      druapp.attachController(controller);

        //      controller.setDisplays(ctxt.getCache());

        controller.initData(dsr);
    }



    public DataviewController getController() {
        return controller;
    }



    public static void main(String[] argv) {
        ReflectionConstructor.addPath("org.catacomb.dataview.build");
        new DataViewer(new File(argv[0]));
    }


}
