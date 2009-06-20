package org.catacomb.dataview.model;


import java.util.ArrayList;

import org.catacomb.dataview.display.Displayable;
import org.catacomb.dataview.display.PolyLine;
import org.catacomb.numeric.data.AsciiIO;
import org.catacomb.numeric.data.DataTable;
import org.catacomb.report.E;

import java.io.File;


public class LineSet extends Plottable {

    public ArrayList<Displayable> getDisplayables(File fdir) {
        ArrayList<Displayable> ret = new ArrayList<Displayable>();

        File f = new File(fdir, file);
        if (!f.exists()) {
            E.warning("no such file " + f);
            return ret;
        }


        DataTable tbl = AsciiIO.readTable(f);

        int ncol = tbl.getNColumn();
        double[] sf = getScaleFactors(ncol);


        double[] xpts = scaleColumn(tbl.getColumn(0), sf[0]);

        if (function == null || function.length() == 0) {
            for (int i = 1; i < ncol; i++) {
                PolyLine pline = new PolyLine("tbl", getColor(),
                                              xpts, scaleColumn(tbl.getColumn(i), sf[i]), width);
                ret.add(pline);
            }
        } else if (function.equals("mean")) {
            double[] ypts = new double[xpts.length];
            for (int i = 1; i < ncol; i++) {
                double[] c = scaleColumn(tbl.getColumn(i), sf[i]);
                for (int j = 0; j < xpts.length; j++) {
                    ypts[j] += c[j];
                }
            }
            for (int j = 0; j < xpts.length; j++) {
                ypts[j] /= (ncol - 1);
            }
            PolyLine pline = new PolyLine("tbl", getColor(), xpts, ypts, width);
            ret.add(pline);

        } else {
            E.error("unrecognized function: " + function + " (only 'mean' is allowed so far)");


        }


        return ret;
    }
}
