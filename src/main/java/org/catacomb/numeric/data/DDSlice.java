package org.catacomb.numeric.data;

import java.lang.reflect.Field;

import org.catacomb.interlish.content.BasicTouchTime;
import org.catacomb.report.E;


import java.util.ArrayList;

public class DDSlice extends StackMultipleSlice implements NumVectorSet {


    String[] elementNames;
    double[][] data;

    int npcache;




    public DDSlice(BlockStack bs, String fnm, Field f, String u, String t, String[] sa) {
        super(bs, fnm, f, u, t);
        elementNames = sa;
        npcache = 0;
        data = new double[10][];
    }


    public BasicTouchTime getTouchTime() {
        return blockStack.getChangeTime();
    }


    public String[] getNames() {
        return elementNames;    // TODO use these;
    }


    public double[] getData() {
        E.warning("shouldn't be calling ...");
        return null;
    }

    public double[][] getDData() {
        if (upToDate()) {
            // nothing to do;

        } else {
            int np = blockStack.getSize();
            if (np > data.length) {
                double[][] dn = new double[np + np/2 + 10][];
                for (int i = 0; i < npcache; i++) {
                    dn[i] = data[i];
                }
                data = dn;
            }

            try {

                for (int i = npcache; i < np; i++) {
                    data[i] = (double[])(field.get(blockStack.getBlock(i)));
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


    public ArrayList<NumVector> getVectors() {
        ArrayList<NumVector> ret = new ArrayList<NumVector>();
        double[][] dd = getDData();
        if (npcache > 0) {
            if (dd[0] == null) {
                E.error("no data in slcice? " + getName() + " " + getLabel());

            } else {
                int nvec = dd[0].length;
                for (int i = 0; i < nvec; i++) {

                    String snm = null;
                    if (elementNames != null && elementNames.length > i) {
                        snm = elementNames[i];
                    }
                    if (snm == null) {
                        snm = getName() + " " + i;
                    }

                    ret.add(new DDSubSlice(this, i, snm));
                }
            }
        }
        return ret;
    }


    public ArrayList<NumVector> getByIndex(int[] ia) {
        ArrayList<NumVector> ret = new ArrayList<NumVector>();
        ArrayList<NumVector> av = getVectors();
        if (ia == null) {
            ret.addAll(av);

        } else {
            for (int i : ia) {
                ret.add(av.get(i));
            }
        }
        return ret;
    }



}
