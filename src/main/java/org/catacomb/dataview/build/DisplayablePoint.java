

package org.catacomb.dataview.build;

import org.catacomb.datalish.Box;
import org.catacomb.graph.gui.Painter;
import org.catacomb.numeric.data.DataExtractor;



public class DisplayablePoint extends Displayable {


    public int size;

    double xpt;
    double ypt;

    boolean hasData = false;





    public void markNeeded(DataExtractor dex) {
        String xf = x.replaceAll("frame", "-1");
        String yf = y.replaceAll("frame", "-1");

        dex.mark(xf);
        dex.mark(yf);
    }



    public void getData(DataExtractor dex, int iframe) {
        String sif = "" + iframe;
        String xf = x.replaceAll("frame", sif);
        String yf = y.replaceAll("frame", sif);

        xpt = dex.getScalar(xf);
        ypt = dex.getScalar(yf);
        hasData = true;
    }


    public void pushBox(Box b) {
        if (hasData) {
            b.extendTo(xpt, ypt);
        }
    }



    public void instruct(Painter p) {
        if (hasData) {
            p.setColor(p_color);
            p.fillRectangle(xpt, ypt, p_color, size);
        }
    }




}
