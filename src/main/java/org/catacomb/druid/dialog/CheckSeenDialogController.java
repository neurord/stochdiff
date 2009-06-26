package org.catacomb.druid.dialog;

import org.catacomb.druid.build.Druid;
import org.catacomb.druid.gui.base.DruInfoPanel;
import org.catacomb.interlish.annotation.Editable;
import org.catacomb.interlish.annotation.IOPoint;
import org.catacomb.interlish.content.BooleanValue;
import org.catacomb.interlish.service.AppPersist;
import org.catacomb.interlish.structure.*;
import org.catacomb.report.E;



public class CheckSeenDialogController  implements Controller {

    Druid druid;

    @IOPoint(xid="MessageArea")
    public DruInfoPanel druInfoPanel;

    @Editable(xid="dontShowAgain")
    public BooleanValue  dontShowAgainBV;

//  String lastShownLabel;



    public CheckSeenDialogController() {
        super();

        druid = new Druid("CheckSeenDialog");
        druid.buildGUI();
        druid.attachSingleController(this);
    }



    public void attached() {
    }

    public void show(Object obj) {
    }


    private void showAt(int x, int y) {
        FrameShowable fs = druid.getFrameShowable();
        fs.pack();
        int[] wh = fs.getSize();
        fs.setLocation(x - wh[0] / 2, y - wh[1] + 20);
        fs.show();
    }




    public void showIfNotYetSeen(String label, String s, int[] xy) {
        if (AppPersist.hasValue("Seen", label)) {
            // dont show it;

            E.info("not showing panel - already shown");

        } else {
            // lastShownLabel = label;
            druInfoPanel.setText(s);
            showAt(xy[0], xy[1]);
        }
    }



    public void OK() {
        if (dontShowAgainBV.getBoolean()) {
            //	 Sys.getSettings().addElement("Seen", lastShownLabel);
            E.info("setting dsag flag");
        }
        druid.hide();
    }


}
