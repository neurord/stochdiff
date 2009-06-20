package org.catacomb.numeric.data;

import org.catacomb.interlish.content.BasicTouchTime;
import org.catacomb.report.E;


public class DDSubSlice implements NumVector {

    DDSlice srcSlice;
    int index;
    String label;

    int npcache;
    double[] data;


    BasicTouchTime touchTime;

    public DDSubSlice(DDSlice slice, int i, String lab) {
        srcSlice = slice;
        index = i;
        label = lab;
        touchTime = new BasicTouchTime();
        touchTime.never();
        data = new double[10];
    }


    public String toString() {
        E.warning("using toString ? ");
        return label;
    }

    public String getName() {
        return label;
    }

    public int getNPoint() {
        return srcSlice.getNPoint();
    }

    public void clear() {
        npcache = 0;
        touchTime.never();
    }


    public double[] getData() {

        if (touchTime.isBefore(srcSlice.getTouchTime())) {

            int npn =  srcSlice.getNPoint();

            if (npn > data.length) {
                double[] dn = new double[(3 * npn) / 2 + 10];
                for (int i = 0; i < npcache; i++) {
                    dn[i] = data[i];
                }
                data = dn;
            }

            double[][] dd = srcSlice.getDData();
            for (int i = npcache; i < npn; i++) {
                data[i] = dd[i][index];
            }
            npcache = npn;

            touchTime.now();
        }

        return data;
    }


    public String getLabel() {
        return label;
    }


    public String getUnit() {
        return srcSlice.getUnit();
    }

}
