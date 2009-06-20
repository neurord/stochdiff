package org.catacomb.druid.dialog;

import org.catacomb.druid.gui.base.DruInfoPanel;
import org.catacomb.interlish.annotation.IOPoint;



public class ConfirmationDialogController extends DialogController {

    @IOPoint(xid="info")
    public DruInfoPanel infoPanel;

    boolean returnValue;



    public void yes() {
        returnValue = true;
        hideDialog();
    }

    public void cancel() {
        returnValue = false;
        hideDialog();
    }


    public boolean getResponse(int[] xy, String msg) {
        returnValue = false;
        checkInit();

        infoPanel.showInfo(msg);
        infoPanel.revalidate();
        showModalAt(xy[0], xy[1]);

        return returnValue;
    }

}
