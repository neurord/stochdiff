
package org.catacomb.druid.blocks;

import org.catacomb.druid.build.Context;
import org.catacomb.druid.build.GUIPath;
import org.catacomb.druid.gui.base.DruDrawingCanvas;
import org.catacomb.druid.gui.base.DruPanel;


public class Canvas extends Panel {


    public int width;
    public int height;



    public DruPanel instantiatePanel() {

        if (width <= 0) {
            width = 200;
        }
        if (height <= 0) {
            height = 100;
        }

        return new DruDrawingCanvas(width, height);
    }


    public void populatePanel(DruPanel drup, Context ctx, GUIPath gpath) {


    }




}
