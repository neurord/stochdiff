package org.catacomb.druid.dialog;

import org.catacomb.druid.gui.base.DruInfoPanel;
import org.catacomb.druid.gui.base.DruScrollingInfoPanel;
import org.catacomb.interlish.annotation.IOPoint;
import org.catacomb.interlish.report.Logger;
import org.catacomb.interlish.report.Message;


public class ProgressLogDialogController extends DialogController
    implements Logger {


    @IOPoint(xid="headInfo")
    public DruInfoPanel headInfoPanel;

    @IOPoint(xid="logInfo")
    public DruScrollingInfoPanel logInfoPanel;





    public void close() {

        hideDialog();
    }


    public void show() {
        show(null);
    }


    public void show(int[] xyin) {
        int[] xy = xyin;
        checkInit();

        if (xy == null) {
            xy = new int[2];
            xy[0] = 400;
            xy[1] = 400;
        }

        showModalAt(xy[0], xy[1]);
    }


    public void log(String s) {
        logInfoPanel.showInfo(s);
    }


    public void log(Message m) {
        logInfoPanel.showInfo(m.getSummary());
    }


    public void optionalIncrementLog(int ifr, String string) {
        String txt = "" + ifr + " " + string;
        logInfoPanel.showInfo(txt);
    }


    public void init(String string) {
        headInfoPanel.showInfo(string);
        //  headInfoPanel.revalidate();
        showNonModalAt(400, 400);
    }


    public void end() {
        hideDialog();
    }

}
