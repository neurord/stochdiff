package org.catacomb.dataview;

import java.io.File;

import org.catacomb.dataview.gui.DViewController;
import org.catacomb.druid.build.Druid;
import org.catacomb.druid.load.DruidAppBase;
import org.catacomb.druid.load.DruidResourceLoader;
import org.catacomb.interlish.service.ResourceAccess;
import org.catacomb.interlish.service.ResourceLoader;
import org.catacomb.report.E;
import org.catacomb.util.FileUtil;


public class DView {

    DViewController controller;

    static boolean doneInit = false;


    public static void main(String[] argv) {

        if (argv.length == 1) {
            File f = new File(argv[0]);
            /*
             if (argv[0].indexOf("/") == 0) {
                f = new File(argv[0]);
             } else {

             }
             */
            String s = FileUtil.readStringFromFile(f);
            E.info("file spec " + s);

            DView dview = new DView(f);
            dview.makeImages(f.getParentFile());
        } else {
            E.info("USAGE: dview file");
        }
    }





    public DView(File f) {
        if (!doneInit) {
            doneInit = true;
            DruidAppBase.init("dview", new CCVizRoot());
        }

        ResourceLoader rl = new DruidResourceLoader();
        ResourceAccess.setResourceLoader(rl);


        String configPath = "org.catacomb.dataview.gui.DView";

        // most of the work is done by the druid
        Druid druid = new Druid(configPath);

        druid.whizzBang();

        // REFAC messsy - just druid.show(); ??

        controller = (DViewController)druid.getController();
        controller.open(f);

        druid.packShow();
    }


    public void makeImages(File fdirin) {
        File fdir = fdirin;
        if (!fdir.isDirectory()) {
            fdir = fdir.getAbsoluteFile().getParentFile();
        }
        controller.makeImages(fdir);
    }


}
