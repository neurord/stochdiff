
package org.catacomb.druid.blocks;

import org.catacomb.druid.build.Context;
import org.catacomb.druid.build.Druid;
import org.catacomb.druid.build.GUIPath;
import org.catacomb.druid.gui.base.DruMovieDisplay;
import org.catacomb.druid.gui.base.DruPanel;
import org.catacomb.interlish.structure.ModeController;
import org.catacomb.interlish.structure.MovieController;
import org.catacomb.report.E;



public class MovieDisplay extends Panel {

    public int width;
    public int height;

    public String controls;

    public MovieDisplay() {

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

        return new DruMovieDisplay(width, height);
    }



    public void populatePanel(DruPanel dpp, Context ctx, GUIPath gpath) {

        DruMovieDisplay drap= (DruMovieDisplay)dpp;

        String ctrlpan = null;
        if (controls == null || controls.equals("true")) {
            ctrlpan = "org.catacomb.druid.chunk.MovieControls";

        } else {
            E.warning("unrecognized ctrl style " + controls);
        }


        if (ctrlpan != null) {
            Druid druid = new Druid(ctrlpan, ctx);
            druid.whizzBang();
            DruPanel dp = druid.getMainPanel();
            drap.addSouth(dp);
            drap.setMovieController((MovieController)druid.getController());
        }



        String modepan = "org.catacomb.druid.chunk.MouseModes";

        if (modepan != null) {
            Druid druid = new Druid(modepan, ctx);
            druid.whizzBang();
            DruPanel dp = druid.getMainPanel();
            drap.addNorth(dp);
            drap.setModeController((ModeController)druid.getController());
        }



    }

}
