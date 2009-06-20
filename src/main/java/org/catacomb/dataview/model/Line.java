package org.catacomb.dataview.model;

import java.util.ArrayList;

import org.catacomb.dataview.display.Displayable;
import org.catacomb.dataview.display.PolyLine;
import org.catacomb.numeric.data.AsciiIO;
import org.catacomb.numeric.data.DataTable;
import org.catacomb.report.E;

import java.io.File;

public class Line extends Plottable {


    public ArrayList<Displayable> getDisplayables(File fdir) {
        ArrayList<Displayable> ret = new ArrayList<Displayable>();

        File f = new File(fdir, file);
        if (!f.exists()) {
            E.info("Problem reading line data: no such file " + f);
            return ret;
        }


        DataTable tbl = AsciiIO.readTable(f);

        double[] sf = getScaleFactors(2);

        PolyLine pline = new PolyLine("tbl", getColor(),
                                      scaleColumn(tbl.getColumn(0), sf[0]),
                                      scaleColumn(tbl.getColumn(1), sf[1]), width);
        ret.add(pline);

        return ret;
    }



}
