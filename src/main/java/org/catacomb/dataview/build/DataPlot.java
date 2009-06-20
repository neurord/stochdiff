

package org.catacomb.dataview.build;


import org.catacomb.dataview.DataPlotPainter;
import org.catacomb.druid.build.Context;
import org.catacomb.druid.gui.base.DruDataDisplay;
import org.catacomb.druid.gui.base.DruPanel;
import org.catacomb.interlish.structure.AddableTo;
import java.util.ArrayList;




public class DataPlot extends DVPanel implements AddableTo {

    public ArrayList<Displayable> displayList;

    public Axis xAxis;
    public Axis yAxis;


    public void add(Object obj) {
        if (obj instanceof Displayable) {
            if (displayList == null) {
                displayList = new ArrayList<Displayable>();
            }
            displayList.add((Displayable)obj);

        } else {
            System.out.println("data view cant add " + obj);
        }
    }


    public DruPanel makePanel(Context ctxt) {
        DruDataDisplay ddp = new DruDataDisplay(width, height);

        ddp.setXAxis(xAxis.label, xAxis.min, xAxis.max);
        ddp.setYAxis(yAxis.label, yAxis.min, yAxis.max);


        DataPlotPainter dpp = new DataPlotPainter(displayList);
        ddp.attachGraphicsController(dpp);


        ctxt.addToCache(ddp);
        ctxt.addToCache(dpp); // POSERR ??? maybe OK

        /*
        if (xrange != null && xrange.length == 2) {
        ddp.setXRange(xrange[0], xrange[1]);
            }

            if (yrange != null && yrange.length == 2) {
         ddp.setYRange(yrange[0], yrange[1]);
            }
            */
        return ddp;
    }


}
