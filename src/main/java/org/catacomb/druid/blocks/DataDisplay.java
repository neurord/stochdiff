
package org.catacomb.druid.blocks;

import org.catacomb.druid.build.Context;
import org.catacomb.druid.build.Druid;
import org.catacomb.druid.build.GUIPath;
import org.catacomb.druid.gui.base.DruDataDisplay;
import org.catacomb.druid.gui.base.DruPanel;
import org.catacomb.interlish.structure.ModeController;
import org.catacomb.report.E;



public class DataDisplay extends Panel {

    public int width;
    public int height;

    public String controls;

    public DataDisplay() {

    }




    public DruPanel instantiatePanel() {
        if (width <= 0) {
            width = 200;
        }
        if (height <= 0) {
            height = 200;
        }
        if (prefWidth <= 0) {
            prefWidth = width;
        }
        if (prefHeight <= 0) {
            prefHeight = height;
        }

        return new DruDataDisplay(width, height);
    }



    public void populatePanel(DruPanel dpp, Context ctx, GUIPath gpath) {

        DruDataDisplay drap= (DruDataDisplay)dpp;

        String ctrlpan = null;
        if (controls == null || controls.equals("true")) {
            ctrlpan = "org.catacomb.druid.chunk.MouseModes";

        } else if (controls.equals("false") || controls.equals("none")) {
            ctrlpan = "org.catacomb.druid.chunk.CompactMouseModes";

        } else if (controls.equals("tiny")) {
            ctrlpan = "org.catacomb.druid.chunk.CompactMouseModes";

        } else {
            E.warning("unrecognized ctrl style " + controls);
        }


        if (ctrlpan != null) {
            Druid druid = new Druid(ctrlpan, ctx);
            druid.whizzBang();
            DruPanel dp = druid.getMainPanel();
            drap.addNorth(dp);
            drap.setModeController((ModeController)druid.getController());
        }
    }

}
