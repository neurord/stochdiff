package org.catacomb.act;

import java.util.ArrayList;


public class CaseRelayBody extends MethodBody {

    ArrayList<String[]> matchCalls;
    String tstfield;


    public CaseRelayBody(String s) {
        tstfield = s;
        matchCalls = new ArrayList<String[]>();
    }



    public String write() {
        StringBuffer sb = new StringBuffer();

        sb.append("   ");
        for (String[] sa : matchCalls) {
            String sm = sa[0];
            String sc = sa[1];
            sb.append("if (" + tstfield + ".equals(\"" + sm + "\")) {\n");
            sb.append("         " + sc + ";\n");
            sb.append("    } else ");
        }
        sb.append("{\n");

        sb.append("  RunError.error(\"dropped event (no match for  \" + " +
                  tstfield + " + \") in \" + this);\n");

        sb.append("   }\n");


        /*
        sb.append("   switch (" + tstfield + ") {\n");

        for (String[] sa : matchCalls) {
           String sm = sa[0];
           String sc = sa[1];
           sb.append ("      case \"" + sm + "\" :\n");
           sb.append("          " + sc + ";\n");
           sb.append("          break;\n");
        }
        sb. append("      defalut :\n");
        sb.append("          System.out.println(\"warning - dropped event \" + " + tstfield + ");\n");

        sb.append ("   }\n");
        */

        //    E.info("case relay body written " + sb.toString());

        return sb.toString();
    }




    public void addCase(String sid, String call) {
        String[] sa = {sid, call};
        matchCalls.add(sa);
    }

}
