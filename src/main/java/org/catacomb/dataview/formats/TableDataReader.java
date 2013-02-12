package org.catacomb.dataview.formats;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.catacomb.report.E;


// REFAC - replace much of this with methods from new util.AsciiIO class

public class TableDataReader implements DataReader {

    TableDataHandler handler;

    int ncol;
    String[] columnNames;
    ArrayList<double[]> data;

    public TableDataReader(TableDataHandler tdh) {
        handler = tdh;
        data = new ArrayList<double[]>();
    }


    public boolean canRead(String line) {
        boolean ret = false;
        if (line.startsWith("tableColumnNames") || line.startsWith("tableRow")) {
            ret = true;
        }
        return ret;
    }


    public void readBlock(String line, BufferedReader br) {

        if (line.startsWith("tableColumnNames")) {
            StringTokenizer st = new StringTokenizer(line, " ");
            st.nextToken(); // column
            st.nextToken(); // the table name - for now assume only one;
            ncol = Integer.parseInt(st.nextToken());

            columnNames = new String[ncol];
            int nread = 0;
            while (nread < ncol) {


                try {
                    String sline = br.readLine();


                    StringTokenizer xst = new StringTokenizer(sline);
                    while (xst.hasMoreTokens() && nread < ncol) {
                        columnNames[nread] = xst.nextToken();
                        nread += 1;
                    }
                } catch (Exception ex) {
                    E.error("cannot read line needed in table data reader " + ex);
                }

            }


        } else if (line.startsWith("tableRow")) {
            StringTokenizer st = new StringTokenizer(line, " ");
            st.nextToken(); // column
            st.nextToken(); // the table name - for now assume only one;
            int ndat = Integer.parseInt(st.nextToken());

            double[] dat = new double[ndat];
            int nread = 0;
            try {
                while (nread < ncol) {
                    String sline = br.readLine();
                    StringTokenizer xst = new StringTokenizer(sline);
                    while (xst.hasMoreTokens() && nread < ncol) {
                        dat[nread] = Double.parseDouble(xst.nextToken());
                        nread += 1;
                    }
                }
            } catch (Exception ex) {
                E.error("read error " + ex);
            }
            if (dat.length != ncol) {
                E.error("wrong length data in table reader: need " + ncol + " but got " + dat.length);
            } else {
                data.add(dat);
            }


        } else {
            E.error("cannot read " + line);
        }

    }




    public void fix() {
        handler.setColumnNames(columnNames);

        double[][] db = new double[ncol][data.size()];
        for (int i = 0; i < data.size(); i++) {
            double[] dat = data.get(i);
            for (int j = 0; j < ncol; j++) {
                db[j][i] = dat[j];
            }
        }
        handler.setData(db);
    }





}
