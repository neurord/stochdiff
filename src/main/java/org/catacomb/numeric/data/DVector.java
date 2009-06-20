package org.catacomb.numeric.data;

import org.catacomb.interlish.structure.TreeNode;


public class DVector implements TreeNode, NumVector {

    Object parent;

    String id;

    int ndat;
    double[] dat;


    public DVector(Object par, String s) {
        parent = par;
        id = s;
        ndat = 0;
        dat = new double[10];

    }






    // REFAC - shouldn't use
    public String toString() {
        return id;
    }


    public int getNPoint() {
        return ndat;
    }

    public double[] getData() {
        return dat;
    }

    public void add(double d) {
        if (ndat == dat.length) {
            double[] da = new double[2 * ndat];
            for (int i = 0; i < ndat; i++) {
                da[i] = dat[i];
            }
            dat = da;
        }
        dat[ndat++] = d;
    }

    public Object getParent() {
        return parent;
    }

    public int getChildCount() {
        return 0;
    }

    public Object getChild(int index) {
        return null;
    }

    public int getIndexOfChild(Object child) {
        return 0;
    }

    public boolean isLeaf() {
        return true;
    }

    public String getUnit() {
        return "err"; // TODO
    }

    public String getName() {
        return id;
    }

    public String getLabel() {
        return id;
    }


}
