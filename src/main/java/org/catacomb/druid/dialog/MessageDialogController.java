package org.catacomb.druid.dialog;

import org.catacomb.druid.gui.base.DruInfoPanel;
import org.catacomb.druid.gui.base.DruLabelPanel;
import org.catacomb.interlish.annotation.IOPoint;


public class MessageDialogController extends DialogController {

    @IOPoint(xid="title")
    public DruLabelPanel titleLabel;

    @IOPoint(xid="info")
    public DruInfoPanel infoPanel;



    public void OK() {

        hideDialog();
    }



    public void show(int[] xyin, String title, String msg) {
        int[] xy = xyin;
        checkInit();

        if (xy == null) {
            xy = new int[2];
            xy[0] = 400;
            xy[1] = 400;
        }

        titleLabel.setText("<html><b>" + title + "</b></html>");

        infoPanel.showInfo(msg);
        infoPanel.revalidate();

        showAt(xy[0], xy[1]);

    }

}
