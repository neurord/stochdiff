package org.textensor.report;


public class Debug {



    public static void dump(String s, double[] d) {
        String ret = s;
        ret += (" " + d.length + " ");
        for (int i = 0; i < d.length && i < 10; i++) {
            ret += (" " + d[i]);
        }
        System.out.println(ret);
    }



}
