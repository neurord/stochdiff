package org.catacomb.util;

import org.catacomb.report.E;


public class NumUtil {



    public static int getInt(String s, int ierr) {
        int ret = ierr;

        try {
            int ir = Integer.parseInt(s);
            ret = ir;
        } catch (Exception ex) {
            E.warning("cannot parse int " + s);
        }

        return ret;

    }


    public static double getDouble(String s, double err) {
        double ret = err;

        try {
            Double d = new Double(s);
            ret = d.doubleValue();
        } catch (Exception ex) {
            E.warning("cannot parese double " + s);
        }
        return ret;
    }


    public static String print(int i) {
        return "" + i;
    }


    public static String print(double d) {
        return String.format("%.2g", new Double(d));
    }


    public static String print(double d, double delta) {
        double ad = Math.abs(d);
        double adelta = Math.abs(delta);

        String sret = null;
        if (ad < 1.e-10) {
            sret = "0.0";
        } else {
            int nfig = (int)(Math.log(ad / adelta) / 2.3 + 2.);
            nfig = (nfig < 2 ? 2 : (nfig > 6 ? 6 : nfig));
            sret = String.format("%." + nfig + "g", new Double(d));
        }
        return sret;
    }


    public static String format(double d, int nfig) {
        return format(new Double(d), nfig);
    }

    public static String format(Double d, int nfig) {
        return String.format("%." + nfig + "g", d);
    }

}
