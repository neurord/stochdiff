package org.catacomb.util;

import org.catacomb.report.E;

import java.util.zip.CRC32;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.util.ArrayList;

import java.util.StringTokenizer;


public class StringUtil {



    public static String readableRegularize(String sin) {
        String s = sin;


        s = s.replaceAll("[\\(\\),;\\./\\\"\\\'\\\\\\[\\]]", "");
        s = s.replaceAll("[:&\\.]", "-");
        s = s.replaceAll("[=\\{\\}\\?\\<\\>]", "_");
        s = s.replaceAll("%20", "");

        E.info("string regularization: ");
        E.info("in=" + sin);
        E.info("out=" + s);


        return s;

    }


    public static String regularize(String s) {
        CRC32 crc = new CRC32();
        crc.update(s.getBytes());
        long lval = crc.getValue();
        String sval = "" + lval;
        return sval;
    }


    public static String lastCapitalized(String src) {
        String ret = "";
        Pattern pat = Pattern.compile(".*([A-Z][a-z]+)");

        Matcher matcher = pat.matcher(src);
        if (matcher.find()) {
            ret = matcher.group(1);
        } else {
            E.warning("no capitalized sections in " + src);
        }
        return ret;

    }



    public static void main(String[] argv) {
        /*
         String[] sa = {"VoltageDepTrans", "VoltageTrans", "Reader", "timePoint", "_Thing"};

        for (String s : sa) {
           E.info("" + s + " gives " + lastCapitalized(s));
        }
        */

        String[] sa = {"cpts.org.eng.misc.thing1", "cpts.org.eng.misc.thing2",
                       "cpts.org.eng.thing3", "cpts.org.neuro.nthing1", "cpts.org.neuro.nthing2",
                       "cpts.org.newneuro.this.that.thingn"
                      };

        StringTree stree = treeify(sa, "tst");
        stree.print();
    }


    public static String[] nonTrivialComponents(String[] sa) {
        ArrayList<String> al = new ArrayList<String>();
        for (String s : sa) {
            if (s != null) {
                s = s.trim();
                if (s.length() > 0) {
                    al.add(s);
                }
            }
        }
        return al.toArray(new String[0]);
    }


    public static StringTree treeify(String[] sa, String rtnm) {
        StringTree root = new StringTree("", rtnm);
        root.setExcluded();

        for (String s : sa) {
            StringTokenizer st = new StringTokenizer(s, "\\.");
            root.addFromTokens(st);
        }

        // root.compress();

        return root;
    }

    public static StringTree flatTreeify(String[] sa, String rtnm) {
        StringTree root = new StringTree("", rtnm);
        root.setExcluded();

        for (String s : sa) {
            StringTokenizer st = new StringTokenizer(s, "\\.");
            root.addFromTokens(st);
        }

        root.compress();

        root.partialFlatten();

        return root;
    }





    public static String[] copyArray(String[] sa) {
        String[] ret = null;
        if (sa != null) {
            ret = new String[sa.length];
            for (int i = 0; i < sa.length; i++) {
                ret[i] = sa[i];
            }
        }
        return ret;
    }


    public static String semiHTMLize(String s) {
        if (s == null) {
            return "";
        }

        StringTokenizer st = new StringTokenizer(s, "\n");
        StringBuffer ret = new StringBuffer();
        ret.append("<p>\n");
        boolean jdp = false;
        while (st.hasMoreTokens()) {
            String sl = st.nextToken();
            if (sl.trim().length() == 0) {
                if (jdp) {
                    // just done a para beak;
                } else {
                    jdp = true;
                    ret.append("</p><p>\n");
                }

            } else {
                while (sl.startsWith(" ")) {
                    ret.append("&nbsp;");
                    sl = sl.substring(1, sl.length());
                }
                jdp = false;
                ret.append(sl);
                ret.append("<br>\n");
            }
        }
        ret.append("</p>\n");
        return ret.toString();
    }


    public static Double extractQuotedDoubleField(String sin, String fnm) {
        String s = sin;
        Double ret = null;
        s = " " + s;
        String ks = fnm + "=\"";
        int iks = s.indexOf(ks);
        if (iks > 0) {
            int ike = s.indexOf("\"", iks+1);
            if (ike > 0) {
                ret = new Double(s.substring(iks+1, ike));
            }
        }
        E.info("got double field for " + fnm + " from " + s  + " as " + ret);
        return ret;
    }


    public static Double extractDoubleField(String sin, String fnm) {
        String s = sin;
        Double ret = null;
        s = " " + s + " ";
        String ks = fnm + "=";
        int iks = s.indexOf(ks);
        if (iks > 0) {
            int ike = s.indexOf(" ", iks+1);
            if (ike > 0) {
                ret = new Double(s.substring(iks + ks.length(), ike));
            }
        }
        //  E.info("got double field for " + fnm + " from " + s  + " as " + ret);
        return ret;
    }


    public static String capitalize(String s) {
        return s.substring(0,1).toUpperCase() + s.substring(1, s.length());
    }


    public final static int countLines(String stxt) {
        int lineCount = 0;
        for (int i = 0; i < stxt.length(); i++) {
            char ch = stxt.charAt(i);
            if (ch == '\n') {
                lineCount ++;
            }
        }
        return lineCount;
    }

}
