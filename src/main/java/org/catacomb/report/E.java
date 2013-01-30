package org.catacomb.report;

import java.util.HashSet;

public class E {

    public static long time0 = 0;

    static String lastShortSource;
    static String cachedAction;
    static String lastErr;
    static int nrep;

    static String possErrText;

    static HashSet<String> sentMessages = new HashSet<String>();


    static Reporter reporter;


    // REFAC  - enum

    static final int INFO = 1;
    static final int WARNING = 2;
    static final int ERROR = 3;
    static final int MESSAGE = 4;
    static final int FATAL = 5;
    static final int DEPRECATED = 6;
    static final int MISSING = 7;


    static String[] levels;

    private static void initLevels() {
        levels = new String[10];
        levels[INFO] = "INFO";
        levels[WARNING] = "WARNING";
        levels[ERROR] = "ERROR";
        levels[MESSAGE] = "MESSAGE";
        levels[FATAL] = "FATAL";
        levels[DEPRECATED] = "DEPRECATED";
        levels[MISSING] = "MISSING";
    }



    public static void setReporter(Reporter r) {
        reporter = r;
    }


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
        report(INFO, s + getShortSource());
    }


    public static void longInfo(String s) {
        report(INFO, s);
        showSource(16);
    }


    public static void message(String s) {
        report(MESSAGE, s);
    }

    public static void oneLineWarning(String s) {
        report(WARNING,  s + getShortSource());
    }


    public static void shortWarning(String s) {
        report(WARNING,  s + getMediumSource());
    }

    public static void shortError(String s) {
        report(ERROR, s + getShortSource());
    }

    public static void medWarning(String s) {
        report(WARNING, s);
        showSource(4);
    }

    public static void warning(String s) {
        report(WARNING, s);
        showSource(12);
    }

    public static void linkToWarning(String s, Object obj) {
        report(WARNING, s);
        String fcn = obj.getClass().getName();
        String scn = fcn.substring(fcn.lastIndexOf(".") + 1, fcn.length());
        System.out.println("  at " + fcn + ".nomethod(" + scn + ".java:1) " + getShortSource());
        // showSource(4);
    }

    public static void error(String s) {
        error(s, -1);
    }

    public static void longError(String s) {
        error(s, 20);
    }


    public static void error(String s, int n) {
        if (lastErr != null && lastErr.equals(s)) {
            nrep += 1;
            if (nrep == 3 || nrep == 10 || nrep ==30 || nrep == 100) {
                report(ERROR, " .......  last error repeated " + nrep + " times");
            }
        } else {
            if (nrep > 0) {
                report(ERROR, "total repeats of last error " + nrep);
            }
            nrep = 0;
            lastErr = s;
            report(ERROR, s);
            if (n < 0) {
                showSource();
            } else {
                showSource(n);
            }
        }
    }


    public static void possibleError(String s) {
        possErrText = "Possible Error " + s + "\n" + getMediumSource();
    }


    public static void debugError(String s) {
        report(ERROR, s);
        System.out.println("stack trace follows: ");
        stackTrace();
    }


    public static void fatalError(String s) {
        report(FATAL, s);
        stackTrace();
        System.exit(3);
    }


    public static void override(String s) {
        report(MISSING, "method should be overridden: " + s);
        showSource();
    }

    public static void override() {
        report(MISSING, "method should be overridden: ");
        showSource();
    }


    public static void deprecate(String s) {
        report(DEPRECATED, "using deprecated class: " + s + " " +
               getShortSource());
        showShortSource();
    }

    public static void deprecate() {
        report(DEPRECATED, "using deprecated method " + getShortSource());
        showSource();
    }

    public static void missing(String s) {
        report(MISSING, "missing code needed: " + s);
        showSource();
    }


    public static void missing() {
        report(MISSING, "missing code needed");
        showSource();
    }


    public static void shortMissing(String s) {
        report(MISSING,  s + getShortSource());
    }

    public static void shortMissingOnce(String s) {
        String msg = "MISSING - " + s + getShortSource();
        if (sentMessages.contains(msg)) {

        } else {
            sentMessages.add(msg);
            report(MISSING, s + getShortSource());
        }
    }

    public static void stackTrace() {
        (new Exception()).printStackTrace();
    }




    public static void showSource() {
        showSource(18);
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



    public static String getMediumSource() {
        String ret = "";
        int n = 2;
        StackTraceElement[] stea = (new Exception()).getStackTrace();
        for (int i = 2; i < 2 + n && i < stea.length; i++) {
            ret += " at " + stea[i].toString() + "\n";
        }
        return ret;
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
        report(INFO, "may relate to: " + cachedAction);
    }


    public static void dump(String[] labs) {
        if (labs != null) {
            for (int i = 0; i < labs.length; i++) {
                E.info("element " + i + ": " + labs[i]);
            }
        }
    }


    public static void dump(String s, int[] ia) {
        E.info("int[] array: " + s + " " + ia.length);
        for (int i = 0; i < ia.length; i++) {
            E.info("   elt " + i + " = " + ia[i]);
        }
    }


    public static void dump(String s, double[] da) {
        E.info("double[] array: " + s + " " + da.length);
        for (int i = 0; i < da.length; i++) {
            E.info("   elt " + i + " = " + da[i]);
        }
    }


    public static String toString(double[] pts) {
        StringBuffer sb = new StringBuffer();
        if (pts == null) {
            sb.append("(null)");
        } else {
            sb.append("[");
            for (int i = 0; i < pts.length; i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(String.format("%.3g", new Double(pts[i])));
            }

            sb.append("]");
        }
        return sb.toString();
    }


    public static void warnOnce(String msg) {
        if (sentMessages.contains(msg)) {

        } else {
            sentMessages.add(msg);
            shortWarning(msg);
        }

    }



    public static void report(int level, String s) {
        if (levels == null) {
            initLevels();
        }
        System.out.println(levels[level] + " - " + s);

        if (reporter != null) {
            if (level == INFO) {
                reporter.reportInfo(s);

            } else if (level == WARNING) {
                reporter.reportWarning(s);

            } else if (level == ERROR) {
                reporter.reportError(s);
            } else {
                reporter.report(levels[level] + "- " + s);
            }

        }
    }



}
