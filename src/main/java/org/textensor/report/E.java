package org.textensor.report;



public class E {

    public static long time0 = 0;

    static String lastShortSource;
    static String cachedAction;
    static String lastErr;
    static int nrep;

    private static long getTime() {
        return System.currentTimeMillis();
    }


    public static void zeroTime() {
        time0 = getTime();
    }

    public static String getStringTime() {
        if (time0 == 0) {
            zeroTime();
        }
        long dt = (getTime() - time0);
        return "" + dt;
    }

    public static void info(String s) {
        System.out.println("INFO - " + s + getShortSource());
    }

    public static void infoTime(String s) {
        System.out.println("INFO - " + s + " at " + getStringTime());
    }


    public static void longInfo(String s) {
        System.out.println("INFO - " + s);
        showSource(16);
    }


    public static void message(String s) {
        System.out.println("MESSAGE - " + s);
    }

    public static void oneLineWarning(String s) {
        System.out.println("WARNING - " + s + getShortSource());
    }


    public static void shortWarning(String s) {
        System.out.println("WARNING - " + s + getShortSource());
    }

    public static void shortError(String s) {
        System.out.println("ERROR - " + s + getShortSource());
    }

    public static void medWarning(String s) {
        System.out.println("WARNING - " + s);
        showSource(4);
    }

    public static void warning(String s) {
        System.out.println("WARNING - " + s);
        showSource(12);
    }

    public static void linkToWarning(String s, Object obj) {
        System.out.println("WARNING - " + s);
        String fcn = obj.getClass().getName();
        String scn = fcn.substring(fcn.lastIndexOf(".") + 1, fcn.length());
        System.out.println("  at " + fcn + ".nomethod(" + scn + ".java:1)");
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
            System.out.println("ERROR - " + s);
            showSource();
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
        System.exit(0);
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
        String ss = (" at " + stea[2].toString());
        if (ss.equals(lastShortSource)) {
            ss = "";
        } else {
            lastShortSource = ss;
        }
        return ss;
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
