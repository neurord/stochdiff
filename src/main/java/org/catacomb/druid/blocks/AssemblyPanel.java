
package org.catacomb.druid.blocks;

import org.catacomb.datalish.SColor;
import org.catacomb.druid.build.Context;
import org.catacomb.druid.build.Druid;
import org.catacomb.druid.build.GUIPath;
import org.catacomb.druid.gui.base.DruAssemblyPanel;
import org.catacomb.druid.gui.base.DruPanel;
import org.catacomb.interlish.structure.ModeController;



public class AssemblyPanel extends Panel {

    public int width;
    public int height;

    public String dataModel;

    public SColor canvasColor;
    public SColor shelfColor;

    public AssemblyPanel() {
        width = 400;
        height = 200;
    }



    public DruPanel instantiatePanel() {
        return new DruAssemblyPanel(width, height, dataModel);
    }

    public void populatePanel(DruPanel dpp, Context ctx, GUIPath gpath) {

        DruAssemblyPanel drap = (DruAssemblyPanel)dpp;


        Druid druid = new Druid("org.catacomb.druid.chunk.MouseModes", ctx);
        druid.whizzBang();
        DruPanel dp = druid.getMainPanel();

        //  E.info("After build " + ctx.getBg() + " " + dp.getBg());

        //dp.setBg(dpp.getBg());

        drap.addNorth(dp);
        drap.setModeController((ModeController)druid.getController());

        if (canvasColor != null) {
            drap.setCanvasColor(canvasColor.getColor());
        }

        if (shelfColor != null) {
            drap.setShelfColor(shelfColor.getColor());
        }
        // E.info("at end " + dp.getBg());
    }

}
