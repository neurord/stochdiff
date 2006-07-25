package org.textensor.xml;


public final class StringEncoder {



    static String xmlEscape(String s) {
        s = s.replaceAll("&", "&amp;");
        s = s.replaceAll("\"", "\\\\\"");
        s = s.replaceAll("\n", "\\\\n\\\\\n");
        return s;
    }


    static String xmlUnescape(String s) {
        s = s.replaceAll("&amp;", "&");
        s = s.replaceAll("\\\\\"", "\"");
        s = s.replaceAll("\n\n", "\n");
        return s;
    }

}
