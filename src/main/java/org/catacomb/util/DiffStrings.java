package org.catacomb.util;


import java.io.StringWriter;
import java.util.StringTokenizer;


public class DiffStrings {


    public static void compare(String s1, String s2) {
        compare(s1, s2, true);
    }


    public static void compareNonWhitespace(String s1, String s2) {
        compare(s1, s2, false);
    }


    public static void compare(String s1, String s2, boolean wsMatters) {
        Object[] sa1 = lineArray(s1, wsMatters);
        Object[] sa2 = lineArray(s2, wsMatters);


        Diff diff = new Diff(sa1, sa2);

        Diff.change script = diff.diff_2(false);
        DiffPrint.Base p = new DiffPrint.UnifiedPrint(sa1, sa2);
        StringWriter wtr = new StringWriter();
        p.setOutput(wtr);
        p.print_script(script);

        String sres = wtr.toString().trim();
        if (sres.length() == 0) {
            System.out.println("Diff: The strings are the same (" + (wsMatters ? "including" : "ignoring") +
                               " whitespace)");
        } else {
            System.out.println("Diff: The strings differ:");
            System.out.println(sres);
        }
    }



    public static Object[] lineArray(String s, boolean wsMatters) {

        StringTokenizer st = new StringTokenizer(s, "\n");
        int nt = st.countTokens();

        Object[] ret = new Object[nt];

        for (int i = 0; i < nt; i++) {
            String tok = st.nextToken();
            if (wsMatters) {
                ret[i] = tok;
            } else {
                ret[i] = new WSString(tok);
            }
        }

        return ret;
    }


}


