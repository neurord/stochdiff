package org.catacomb.numeric.data;

import org.catacomb.report.E;

import java.util.ArrayList;


public class DataTable {

    String id;

    int ncol;

    String[] headings;

    ArrayList<double[]> rows = new ArrayList<double[]>();

    int nrow;
    double[][] columns;



    public void setID(String s) {
        id = s;
    }

    public String getID() {
        return id;
    }

    public void setNColumn(int n) {
        ncol = n;
    }


    public void setHeadings(String[] sa) {
        if (headings != null) {
            E.warning("overwriting headings? " + headings[0] + " with " + sa[0]);
        }
        headings = sa;
    }

    public String[] getHeadings() {
        if (headings == null) {
            headings = new String[ncol];
            for (int i = 0; i < ncol; i++) {
                headings[i] = "C" + i;
            }
        }
        return headings;
    }



    public void addRow(double[] da) {
        rows.add(da);
    }


    public void close() {
        nrow = rows.size();
        columns = new double[ncol][nrow];
        for (int i = 0; i < nrow; i++) {
            double[] row = rows.get(i);
            for (int j = 0; j < ncol; j++) {
                columns[j][i] = row[j];
            }
        }
    }




    public double[] getColumn(int icol) {
        if (columns == null) {
            close();
        }
        return columns[icol];
    }




    public int getNColumn() {
        return ncol;
    }

    public double[][] getRows() {
        if (columns == null) {
            close();
        }
        double[][] ret = new double[nrow][ncol];
        for (int i = 0; i < nrow; i++) {
            for (int j = 0; j < ncol; j++) {
                ret[i][j] = columns[j][i];
            }
        }
        return ret;
    }


    public double[][] getColumns() {
        if (columns == null) {
            close();
        }
        return columns;
    }



}
