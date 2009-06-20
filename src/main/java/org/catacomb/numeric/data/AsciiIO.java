package org.catacomb.numeric.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.StringTokenizer;

import org.catacomb.report.E;

public class AsciiIO {

    public static DataTable readTable(File f) {
        DataTable ret = null;

        int ncol = 0;

        boolean doneHeadings = false;
        if (f.exists()) {
            ret = new DataTable();
            ret.setID(f.getName());

            try {
                BufferedReader br = new BufferedReader(new FileReader(f));
                while (br.ready()) {
                    String line = br.readLine();
                    line = line.trim();
                    if (line.length() > 0) {

                        StringTokenizer st = new StringTokenizer(line, " ,;\t");
                        if (ncol == 0) {
                            ncol = st.countTokens();
                            ret.setNColumn(ncol);
                        }

                        if (st.countTokens() < ncol) {
                            E.warning("too few elements in row - skipping " + line);
                        } else {
                            if (st.countTokens() > ncol) {
                                E.warning("extra tokens in line beyond " + ncol + "? " + line);
                            }

                            if (line.startsWith("#")) {
                                if (!doneHeadings) {
                                    st = new StringTokenizer(line, " ,;\t");
                                    String[] sa = readStringRow(st, ncol);
                                    ret.setHeadings(sa);
                                    doneHeadings = true;
                                }

                            } else {
                                double[] da = readRow(st, ncol);

                                if (da == null) {
                                    E.info("Scrapping " + line);
                                } else {
                                    ret.addRow(da);
                                }
                            }

                        }

                    }
                }
            } catch (Exception ex) {
                E.warning("file read exception for " + f + " " + ex);
                ex.printStackTrace();
            }


        } else {
            E.warning("no such file " + f);
        }
        if (ret != null) {
            ret.close();
        }
        return ret;
    }




    public static double[] readRow(StringTokenizer st, int ncol) {
        double[] ret = new double[ncol];
        try {
            for (int i = 0; i < ret.length; i++) {
                ret[i] = Double.parseDouble(st.nextToken());
            }
        } catch (Exception ex) {
            ret = null;
        }
        return ret;
    }



    public static String[] readStringRow(StringTokenizer st, int ncol) {
        String[] ret = new String[ncol];
        if (st.countTokens() < ncol) {
            E.error("need " + ncol + " but got only " + st.countTokens() + " tokens in " + st);
        } else {
            for (int i = 0; i < ncol; i++) {
                ret[i] = st.nextToken();
            }
        }
        return ret;
    }




    public static double[] readRow(String line) {
        StringTokenizer st = new StringTokenizer(line, " ,;\t[]");
        int nc = st.countTokens();
        return readRow(st, nc);
    }

}
