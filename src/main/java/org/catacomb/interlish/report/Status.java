package org.catacomb.interlish.report;

import org.catacomb.interlish.structure.Producer;
import org.catacomb.interlish.structure.StatusDisplay;
import org.catacomb.interlish.structure.StatusSource;
import org.catacomb.report.E;



public class Status implements StatusSource, Producer {

    StatusDisplay display;

    static Status instance;


    public static void show(String s) {
        getInstance().exportStatus(s);
    }


    public void setStatusDisplay(StatusDisplay sd) {
        display = sd;
    }

    public void exportStatus(String s) {
        if (display == null) {
            E.error("no display in status ?");
        } else {
            display.showStatus(s);
        }
    }


    public static Status getInstance() {
        if (instance == null) {
            instance = new Status();
        }
        return instance;
    }






}
