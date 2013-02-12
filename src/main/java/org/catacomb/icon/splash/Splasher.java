package org.catacomb.icon.splash;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.catacomb.Root;
import org.catacomb.report.E;


public class Splasher {

    static Splasher splasher;

    Splash splash;


    public static void showSplash(String confpath) {
        new Splasher(confpath);
    }


    public static void hideSplash() {
        if (splasher != null) {
            splasher.instanceHideSplash();
        }
    }



    public Splasher(String configPath) {
        // is there a splash screen? if so, pull it out before doing
        // anything else
        String config = getXMLResource(configPath);

        String att = getAttribute("splashScreen", "src", config);

        if (att != null) {
            String pp = parentPath(configPath);
            splash = new Splash(pp, att);

            splash.show();
        }
    }


    public void instanceHideSplash() {
        if (splash != null) {
            splash.hide();
        }
    }



    public static void main(String[] argv) {
        // E.zeroTime();

        showSplash(argv[0]);

    }


    public static String parentPath(String pth) {
        String separator = ".";
        int ils = pth.lastIndexOf(separator);
        String ret = null;
        if (ils > 0) {
            ret = pth.substring(0, ils);
        } else {
            ret = pth;
        }
        return ret;
    }




    public static String getXMLResource(String pathin) {
        String path = pathin;
        String sret = null;

        if (path.endsWith(".xml") || path.indexOf(".") < 0) {
            E.warning("getXMLReousrce should have a dot path, not " + path);

        } else {
            path = path.replaceAll("\\.", "/") + ".xml";
        }
        try {
            String pref = "org/catacomb/";
            if (path.startsWith(pref)) {
                path = path.substring(pref.length(), path.length());
                InputStream fis = (new Root()).getClass().getResourceAsStream(path);
                sret = readInputStream(fis);

            } else {
                InputStream fis = ClassLoader.getSystemResourceAsStream(path);
                sret = readInputStream(fis);
            }

        } catch (Exception ex) {
            E.error("ResourceAccess - cannot get " + path + " " + ex);
            ex.printStackTrace();
        }
        return sret;
    }



    private static String readInputStream(InputStream fis)
    throws NullPointerException, IOException {
        String sret = null;

        InputStreamReader insr = new InputStreamReader(fis);
        BufferedReader fr = new BufferedReader(insr);

        StringBuffer sb = new StringBuffer();
        while (fr.ready()) {
            sb.append(fr.readLine());
            sb.append("\n");
        }
        fr.close();
        sret = sb.toString();

        return sret;
    }



    // following from ElementExtractor in quickxml. Should reduce  for
    // just this use

    private static String getAttribute(String enm, String atnm, String src) {
        String ret = null;

        String etxt = getElementText(enm, src);

        if (etxt != null) {
            ret = getAttribute(atnm, etxt);
        }

        return ret;
    }



    private static String getAttribute(String atnm, String src) {
        String ret = null;
        Pattern pat = Pattern.compile(atnm + "=\"(.*)\"");

        Matcher matcher = pat.matcher(src);
        if (matcher.find()) {
            ret = matcher.group(1);
        }
        return ret;
    }





    private static String getElementText(String enm, String src) {

        String ret = getVerboseElementText(enm, src);

        if (ret == null) {
            ret = getCompactElementText(enm, src);
        }
        return ret;
    }



    private static String getVerboseElementText(String enm, String src) {
        String ret = null;



        Pattern pat = Pattern.compile("<" + enm + ">(.*)</" + enm + ">");

        Matcher matcher = pat.matcher(src);
        if (matcher.find()) {
            ret = matcher.group(1);
        }
        return ret;
    }




    private static String getCompactElementText(String enm, String src) {
        String ret = null;

        Pattern pat = Pattern.compile("<" + enm + " (.*)/>");

        Matcher matcher = pat.matcher(src);
        if (matcher.find()) {
            ret = matcher.group(1);
        }
        return ret;
    }
}
