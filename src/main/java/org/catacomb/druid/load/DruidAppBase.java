package org.catacomb.druid.load;


import java.io.File;

import org.catacomb.interlish.report.Logger;
import org.catacomb.interlish.report.PrintLogger;
import org.catacomb.interlish.service.AppPersist;
import org.catacomb.interlish.service.AppPersistProvider;
import org.catacomb.report.E;


public final class DruidAppBase implements AppPersistProvider {

    static DruidAppBase instance;


    String applicationName;
    Logger logger;
    Settings settings;


    static String defaultApplicationName = "druid-application";



    public static void init() {
        init(null, null);
    }


    public static void init(String s, Object projroot) {
        String apn = null;
        if (s == null || s.length() == 0) {
            apn = defaultApplicationName;
        } else {
            apn = s;
        }

        if (instance == null) {
            instance = new DruidAppBase(apn);
            AppPersist.setProvider(instance);

            DruidContentLoader.initLoader(projroot);



        } else {
            E.debugError("Sys.init called when it has already been initialized");
        }



    }



    public DruidAppBase(String ap) {
        applicationName = ap;

    }



    public static DruidAppBase getSys() {
        if (instance == null) {
            init();
        }
        return instance;
    }





    public String getApplicationName() {
        return applicationName;
    }


    public Settings getSettings() {
        if (settings == null) {
            settings = new Settings(applicationName);
        }
        return settings;
    }







    public Logger getLogger() {
        if (logger == null) {
            logger = new PrintLogger();
        }
        return logger;
    }



    public void requestExit() {

        System.exit(3);


    }





    public boolean hasValueFor(String pel) {
        return getSettings().hasElement(pel);
    }

    public boolean hasValue(String pel, String val) {
        return getSettings().hasElement(pel, val);
    }

    public String getValueFor(String pel) {
        return getSettings().getValue(pel);
    }

    public void addRecentFile(File f) {
        getSettings().addRecentFile(f);
    }

    public void setValue(String lab, String val) {
        getSettings().setElement(lab, val);
    }

    public String[] getRecentPaths() {
        return getSettings().getRecentPaths();
    }


    public void forceExit() {
        System.exit(3);
    }


}
