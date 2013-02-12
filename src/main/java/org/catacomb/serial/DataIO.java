
package org.catacomb.serial;


import org.catacomb.report.E;



public class DataIO {


    public static double[] readDoubleArray(String sin) {
        String s = sin;
        if (s.startsWith("{")) {
            s = s.substring(1, s.indexOf("}"));
        }
        s = s.trim();

        String[] sa = s.split("[ ,\t\n\r]+");

        /*
        E.info("after splitting " + s);
        for (int i = 0; i < sa.length; i++) {
        E.info("item " + i + " " + sa[i]);
            }
            */

        int nt = sa.length;
        double[] value = new double[nt];

        try {
            for (int i = 0; i < nt; i++) {
                value[i] = (new Double(sa[i])).doubleValue();
            }
        } catch (Exception ex) {
            E.error("float reading cannot extract " + nt + " doubles from " + s);
            for (int i = 0; i < nt; i++) {
                E.info("string " + i + "=xxx" + sa[i] + "xxx");
            }
        }

        return value;
    }




}
