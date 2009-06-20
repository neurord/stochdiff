package org.catacomb.interlish.report;

import org.catacomb.interlish.structure.Producer;
import org.catacomb.interlish.structure.StatusDisplay;
import org.catacomb.interlish.structure.StatusSource;
import org.catacomb.report.E;



public class Focus implements StatusSource, Producer {

    StatusDisplay display;

    static Focus instance;


    public static void show(String s) {
        getInstance().exportFocus(s);
    }


    public void setStatusDisplay(StatusDisplay sd) {
        display = sd;
    }

    public void exportFocus(String s) {
        if (display == null) {
            E.error("no display in status ?");
        } else {
            display.showStatus(s);
        }
    }


    public static Focus getInstance() {
        if (instance == null) {
            instance = new Focus();
        }
        return instance;
    }






}
