package org.catacomb.xdoc;

import org.catacomb.interlish.structure.Page;
import org.catacomb.interlish.util.JUtil;

public class HTMLPage implements Page {


    String htmlTemplate;

    String htmlPageText;

    String sourceText;


    public final static int PLAIN_TEXT = 1;
    public final static int  DASSIE_TEXT = 2;

    public int sourceStyle = PLAIN_TEXT;



    public HTMLPage() {
        htmlPageText = "";
    }


    public HTMLPage(String s) {
        sourceText = s;
        htmlPageText = null;
    }

    public HTMLPage(String s, int iss) {
        sourceText = s;
        sourceStyle = iss;
        htmlPageText = null;
    }



    public void setSourceStyle(int iss) {
        sourceStyle = iss;
    }





    public void loadDefault() {
        htmlPageText = JUtil.getRelativeResource(this, "homePageHTML.txt");
    }


    public String getHTMLText() {
        if (htmlPageText == null && sourceText != null) {
            wrapText(sourceText);
        }
        return htmlPageText;
    }

    public void setSourceText(String s) {
        sourceText = s;
        htmlPageText = null;
    }

    public void setSourceDassieText(String s) {
        sourceStyle = DASSIE_TEXT;
        sourceText = s;
        htmlPageText = null;
    }




    private String getHTMLTemplate() {
        if (htmlTemplate == null) {
            htmlTemplate = JUtil.getRelativeResource(this, "baseTemplateHTML.txt");
        }
        return htmlTemplate;
    }



    public void wrapText(String txt) {
        String sr = null;
        if (sourceStyle == PLAIN_TEXT) {
            sr = wrapPlainString(txt);
        } else {
            sr = wrapDassieString(txt);
        }
        wrapHTMLText(sr);
    }






    private String wrapPlainString(String txtin) {
        String txt = txtin;
        StringBuffer textSB = new StringBuffer();

        textSB.append("<p>");

        txt = txt.replaceAll(" -p- ", " </p>\n<p> ");
        txt = txt.replaceAll(" -br- ", " <br>\n ");

        textSB.append(txt);

        textSB.append("</p>");

        return textSB.toString();
    }



    public String wrapDassieString(String txt) {
        //  E.info("tagging " + txt);
        String ret = TextTagger.getTagger().tagText(txt);
        //  E.info("dassie origianal " + txt);
        //  E.info("dassie wrapped tp " + ret);
        return ret;
    }



    public void wrapHTMLText(String s) {
        String txt = getHTMLTemplate();
        int ibo = txt.indexOf("BODY");

        htmlPageText = txt.substring(0, ibo) + s + txt.substring(ibo+4, txt.length());
    }


    public void loadText(String fnm) {
        htmlPageText = JUtil.getRelativeResource(this, fnm);
    }


}
