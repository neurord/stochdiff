package org.catacomb.serial.quickxml;

import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class ElementExtractor {





    public static String getAttribute(String enm, String atnm, String src) {
        String ret = null;

        String etxt = getElementText(enm, src);

        if (etxt != null) {
            ret = getAttribute(atnm, etxt);
        }

        return ret;
    }



    public static String getAttribute(String atnm, String src) {
        String ret = null;
        Pattern pat = Pattern.compile(atnm + "=\"(.*)\"");

        Matcher matcher = pat.matcher(src);
        if (matcher.find()) {
            ret = matcher.group(1);
        }
        return ret;
    }





    public static String getElementText(String enm, String src) {

        String ret = getVerboseElementText(enm, src);

        if (ret == null) {
            ret = getCompactElementText(enm, src);
        }
        return ret;
    }





    public static String getVerboseElementText(String enm, String src) {
        String ret = null;

        Pattern pat = Pattern.compile("<" + enm + ">(.*)</" + enm + ">");

        Matcher matcher = pat.matcher(src);
        if (matcher.find()) {
            ret = matcher.group(1);
        }
        return ret;
    }




    public static String getCompactElementText(String enm, String src) {
        String ret = null;

        Pattern pat = Pattern.compile("<" + enm + " (.*)/>");

        Matcher matcher = pat.matcher(src);
        if (matcher.find()) {
            ret = matcher.group(1);
        }
        return ret;
    }


}
