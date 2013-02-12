package org.catacomb.interlish.version;

import org.catacomb.about.AboutBase;
import org.catacomb.interlish.util.JUtil;
import org.catacomb.report.E;


public class BuildInfo {

    public String name;
    public String num;
    public String time;
    public String date;



    public static BuildInfo getInfo() {
        return new BuildInfo();
    }


    public BuildInfo() {
        String s = JUtil.getRelativeResource(new AboutBase(), "version.xml");
        time = "unknown";
        date = "unknown";
        name = "unknown";
        num = "0";

        if (s != null && s.length() > 0) {
            time = getQuotedText(s, "time=");
            date = getQuotedText(s, "date=");
            num = getQuotedText(s, "num=");
            name = getQuotedText(s, "name=");
        }
    }


    public void setName(String s) {
        name = s;
    }

    public void setNum(String s) {
        num = s;
    }


    private String getQuotedText(String src, String start) {
        String ret = "";
        if (src.indexOf(start) >= 0) {
            String rest = src.substring(src.indexOf(start) + start.length() + 1, src.length());
            if (rest.indexOf("\"") > 0) {
                ret = rest.substring(0, rest.indexOf("\""));
            } else {
                E.warning("no closing quote? - seeking " + start + " in " + src);
            }

        } else {
            E.warning("cannot find " + start + " in " + src);
        }
        return ret;
    }


    public void printIntro() {
        System.out.println(getIntro());
    }

    public String getTitleDate() {
        return  "[" + date + ", " + time +  "]";
    }

    public String getIntro() {
        return name + " " + num + "   [" + date + ", " + time +  "]";
    }


    public String getFrameTitle() {
        return getIntro();
    }

}
