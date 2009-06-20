

package org.catacomb.dataview.build;


import org.catacomb.datalish.Box;
import org.catacomb.datalish.SColor;
import org.catacomb.graph.gui.Painter;
import org.catacomb.numeric.data.DataExtractor;
import org.catacomb.report.E;

import java.awt.Color;



public class Displayable  {

    public String label;
    public SColor color;

    public String x;
    public String y;

    Color p_color;


    private boolean doneErr = false;

    public Color extractColor() {
        return color.getColor();
    }


    public Color getColor() {
        if (p_color == null) {
            p_color = extractColor();
        }
        return p_color;
    }


    public void pushBox(Box b) {
        E.missing("" + b);
    }



    public void markNeeded(DataExtractor dex) {
        E.error("most override mark needed in " + getClass() + " "  + dex);
    }

    public void getData(DataExtractor ex, int iframe) {
        E.error("most override get data in " + getClass() + " " + ex + " "  + iframe);
    }


    public void instruct(Painter p, int widthFactor) {
        if (!doneErr) {
            E.error("most override instruct in " + getClass() + p + " " + widthFactor);
            doneErr = true;
        }
    }

}
