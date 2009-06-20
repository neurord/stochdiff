package org.catacomb.dataview;

import org.catacomb.dataview.gui.CCVizController;
import org.catacomb.druid.build.Druid;
import org.catacomb.druid.load.DruidAppBase;
import org.catacomb.druid.load.DruidResourceLoader;
import org.catacomb.interlish.reflect.ReflectionConstructor;
import org.catacomb.interlish.service.ResourceAccess;
import org.catacomb.interlish.service.ResourceLoader;

import java.io.File;


public class CCViz {


    public static void main(String[] argv) {
        ReflectionConstructor.addPath("org.catacomb.druid.manifest");


        DruidAppBase.init("ccviz", new CCVizRoot());

        String configPath = "org.catacomb.dataview.gui.CCViz";

        ResourceLoader rl = new DruidResourceLoader();
        ResourceAccess.setResourceLoader(rl);


        // most of the work is done by the druid
        Druid druid = new Druid(configPath);

        druid.whizzBang();

        // REFAC messsy - just druid.show(); ??
        if (argv.length > 0) {
            ((CCVizController)druid.getController()).open(new File(argv[0]));
        }
    }



}
