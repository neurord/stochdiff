package org.catacomb.numeric.data;

import java.lang.reflect.Field;

import org.catacomb.report.E;


public class DSlice extends StackSingleSlice implements NumVector {


    double[] data;

    int npcache;


    public DSlice(BlockStack bs, String fnm, Field f, String u, String t) {
        super(bs, fnm, f, u, t);
        npcache = 0;
        data = new double[10];
    }




    public double[] getData() {
        if (upToDate()) {
            // nothing to do;

        } else {
            int np = blockStack.getSize();
            if (np > data.length) {
                double[] dn = new double[np + np/2 + 10];
                for (int i = 0; i < npcache; i++) {
                    dn[i] = data[i];
                }
                data = dn;
            }

            try {

                for (int i = npcache; i < np; i++) {
                    data[i] = field.getDouble(blockStack.getBlock(i));
                }
                npcache = np;

            } catch (Exception ex) {
                E.error("exception reading slice from block stack " + this + " " + ex);
            }

        }
        cacheTime.now();

        return data;
    }


    void clearCache() {
        npcache = 0;
    }





}
