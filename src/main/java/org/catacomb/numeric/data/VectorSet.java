package org.catacomb.numeric.data;

import java.util.ArrayList;




public class VectorSet {

    String[] names;
    int nvec;
    int npoint;

    ArrayList<FloatRow> rows;
    double[][] adat;

    public VectorSet() {
        rows = new ArrayList<FloatRow>();
    }



    public void setNames(String s) {
        names = s.split("[, \n\r\t]+");
        nvec = names.length;
    }


    public void addRow(FloatRow fr) {
        rows.add(fr);
    }


    private void digestRows() {
        npoint = rows.size();
        adat = new double[nvec][npoint];
        int ir = 0;

        for (FloatRow fr : rows) {
            double[] rd = fr.getValue();
            for (int k = 0; k < nvec && k < rd.length; k++) {
                adat[k][ir] = rd[k];
            }
            ir += 1;
        }
    }



    public FloatVector[] getVectors() {
        digestRows();
        FloatVector[] fva = new FloatVector[nvec];
        for (int i = 0; i < nvec; i++) {
            fva[i] = new FloatVector(names[i], adat[i]);
        }
        return fva;
    }


}
