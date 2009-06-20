

package org.catacomb.dataview.build;


import org.catacomb.datalish.Box;
import org.catacomb.graph.gui.Labellee;
import org.catacomb.graph.gui.Painter;
import org.catacomb.numeric.data.DataExtractor;
import org.catacomb.report.E;




public class Line extends Displayable {

    public double width;


    private int npts;
    private double[] xpts;
    private double[] ypts;

    int ilabel = -1;

    Labellee labellee;





    public void markNeeded(DataExtractor dex) {
        String sif = "-1";
        String xf = x.replaceAll("frame", sif);
        String yf = y.replaceAll("frame", sif);

        dex.mark(xf);
        dex.mark(yf);
    }



    public void getData(DataExtractor dex, int iframe) {
        String sif = "" + iframe;
        String xf = x.replaceAll("frame", sif);
        String yf = y.replaceAll("frame", sif);

        xpts = dex.getVector(xf);
        ypts = dex.getVector(yf);

        if (xpts == null || ypts == null) {
            xpts =  null;
            E.error("null data in line - wanted " + x + " and " + y);
        } else {

            npts = xpts.length;
            if (npts == ypts.length) {

            } else {
                int n = ypts.length;
                if (npts > n) {
                    npts = n;
                }

                E.warning("Line: idfferent array lengths " + xpts.length + " and " + ypts.length);
            }
        }
        if (width < 0.5) {
            width =1.;
        }

        if (label != null) {
            if (labellee == null) {
                labellee = new Labellee(xpts, ypts, label, getColor());
            } else {
                labellee.update(xpts, ypts, label, getColor());
            }
        }
    }


    public Labellee getLabellee() {
        return labellee;
    }


    public void pushBox(Box b) {
        for (int i = 0; i < npts; i++) {
            b.extendTo(xpts[i], ypts[i]);
        }
    }



    public void instruct(Painter p, int wf) {
        if (xpts != null) {
            p.setColor(getColor());
            p.drawPolyline(xpts, ypts, npts, getColor(), width * wf, true);

        }

    }

}

