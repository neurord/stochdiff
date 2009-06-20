package org.catacomb.interlish.service;

import java.io.File;

import org.catacomb.report.E;


public class AppPersist {

    static AppPersistProvider approv;

    static String applicationName = "catacomb";


    public static void setProvider(AppPersistProvider app) {
        approv = app;
    }


    public static void checkProvider() {
        if (approv == null) {
            E.warning("no app persist provider");
        }
    }


    public static boolean hasValueFor(String pel) {
        boolean ret = false;
        checkProvider();
        if (approv != null) {
            ret = approv.hasValueFor(pel);
        }
        return ret;
    }

    public static String getValueFor(String pel) {
        String ret = null;
        checkProvider();
        if (approv != null) {
            ret = approv.getValueFor(pel);
        }
        return ret;
    }

    public static void forceExit() {
        checkProvider();
        if (approv != null) {
            approv.forceExit();
        }
    }


    public static void setValue(String lab, String val) {
        checkProvider();
        if (approv != null) {
            approv.setValue(lab, val);
        }

    }


    public static void addRecentFile(File f) {
        checkProvider();
        if (approv != null) {
            approv.addRecentFile(f);
        }

    }


    public static boolean hasValue(String tag, String val) {
        boolean ret = false;
        checkProvider();
        if (approv != null) {
            ret = approv.hasValue(tag, val);
        }
        return ret;
    }


    public void setApplicationName(String s) {
        applicationName = s;
    }

    public static String getApplicationName() {
        return applicationName;
    }


    public static String[] getRecentPaths() {
        String[] ret = new String[0];
        checkProvider();
        if (approv != null) {
            ret = approv.getRecentPaths();
        }
        return ret;
    }





}
