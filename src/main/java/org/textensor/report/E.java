package org.textensor.report;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class E {

    static String cachedAction;
    static String lastErr;
    static int nrep;

    public static void info(String s) {
	Logger log = LogManager.getLogger(getShortSource());
	log.info(s);
    }

    public static void warning(String s) {
	Logger log = LogManager.getLogger(getShortSource());
	log.warn(s);
    }

    public static void _error(String s) {
	Logger log = LogManager.getLogger(getShortSource());
	log.error(s);
    }

    public static void error(String s) {
        if (lastErr != null && lastErr.equals(s)) {
            nrep += 1;
            if (nrep == 3 || nrep == 10 || nrep ==30 || nrep == 100) {
                System.out.println(" .......  last error repeated " + nrep + " times");
            }
        } else {
            if (nrep > 0) {
                System.out.println("total repeats of last error " + nrep);
            }
            nrep = 0;
            lastErr = s;
            _error(s);
        }
    }

    public static void debugError(String s) {
        System.out.println("ERROR - " + s);
        System.out.println("stack trace follows: ");
        stackTrace();
    }


    public static void fatalError(String s) {
        System.out.println("FATAL - " + s);
        stackTrace();
        System.exit(3);
    }


    public static void override(String s) {
        System.out.println("OVERRIDE - method should be overridden: " + s);
        showSource();
    }

    public static void override() {
        System.out.println("OVERRIDE - method should be overridden: ");
        showSource();
    }


    public static void deprecate(String s) {
        System.out.println("DEPRECATED - using deprecated class: " + s + " " +
                           getShortSource());
        showShortSource();
    }

    public static void deprecate() {
        System.out.println("DEPRECATED - using deprecated method " + getShortSource());
        showShortSource();
    }

    public static void missing(String s) {
        System.out.println("MISSING - missing code needed: " + s);
        showSource();
    }


    public static void missing() {
        System.out.println("MISSING - missing code needed");
        showSource();
    }


    public static void stackTrace() {
        (new Exception()).printStackTrace();
    }




    public static void showSource() {
        showSource(10);
    }

    public static void showShortSource() {
        showSource(2);
    }

    public static void showSource(int n) {
        StackTraceElement[] stea = (new Exception()).getStackTrace();
        for (int i = 2; i < 2 + n && i < stea.length; i++) {
            System.out.println("  at " + stea[i].toString());

            /*
              System.out.println("   at " + stea[i].getClassName() +
                     "(" + stea[i].getFileName() + ":" + stea[i].getLineNumber() +")");
            */
        }
    }


    public static String getShortSource() {
        StackTraceElement[] stea = (new Exception()).getStackTrace();
        return stea[2].getClassName() + ":" + stea[2].getMethodName();
    }


    public static void newLine() {
        System.out.println("...");
    }


    public static void cacheAction(String s) {
        cachedAction = s;
    }

    public static void reportCached() {
        System.out.println("may relate to: " + cachedAction);
    }


    public static void dump(String[] labs) {
        if (labs != null) {
            for (int i = 0; i < labs.length; i++) {
                System.out.println("element " + i + ": " + labs[i]);
            }
        }
    }





    public static void dump(String s, String[] labs) {
        System.out.println(s + " " + labs + " " + getShortSource());
        E.dump(labs);
    }

    public static void dump(String s, int[] ia) {
        System.out.println("int[] array: " + s + " " + ia.length + " " + getShortSource());
        for (int i = 0; i < ia.length; i++) {
            System.out.println("   elt " + i + " = " + ia[i]);
        }
    }

    public static void dump(String s, double[] ia) {
        System.out.println("int[] array: " + s + " " + ia.length + " " + getShortSource());
        for (int i = 0; i < ia.length; i++) {
            System.out.println("   elt " + i + " = " + ia[i]);
        }
    }


    public static void dump(String s, double[][] regcon) {
        for (int i = 0; i < regcon.length; i++) {
            dump(s + "-" + i, regcon[i]);
        }
    }


}
