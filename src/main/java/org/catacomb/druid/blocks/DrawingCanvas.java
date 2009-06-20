
package org.catacomb.druid.blocks;

import org.catacomb.datalish.SColor;
import org.catacomb.druid.build.Context;
import org.catacomb.druid.build.GUIPath;
import org.catacomb.druid.gui.base.DruDrawingCanvas;
import org.catacomb.druid.gui.base.DruPanel;


public class DrawingCanvas extends Panel {


    public int width;
    public int height;

    public boolean axes;

    public boolean threeD;

    public SColor gridColor;
    public SColor axisColor;

    public DruPanel instantiatePanel() {

        if (width <= 0) {
            width = 200;
        }
        if (height <= 0) {
            height = 100;
        }

        DruDrawingCanvas ddc = new DruDrawingCanvas(width, height);
        if (axes) {
            ddc.setOnGridAxes();
        }
        if (threeD) {
            ddc.setThreeD();
        }

        return ddc;
    }


    public void populatePanel(DruPanel drup, Context ctx, GUIPath gpath) {
        DruDrawingCanvas ddc = (DruDrawingCanvas)drup;
        if (backgroundColor != null) {
            ddc.setBackgroundColor(backgroundColor.getColor());
        }
        if (gridColor != null) {
            ddc.setGridColor(gridColor.getColor());
        }

        if (axisColor != null) {
            ddc.setAxisColor(axisColor.getColor());
        }
    }

}