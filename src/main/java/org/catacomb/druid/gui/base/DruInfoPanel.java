package org.catacomb.druid.gui.base;

import java.awt.Color;

import org.catacomb.druid.swing.DHTMLPane;
import org.catacomb.interlish.structure.InfoReceiver;
import org.catacomb.interlish.util.JUtil;
import org.catacomb.report.E;
import org.catacomb.xdoc.TextTagger;
import org.catacomb.xdoc.XdocBase;


public class DruInfoPanel extends DruPanel implements InfoDisplay, InfoReceiver {

    static final long serialVersionUID = 1001;

    String htmlTemplate;

    DHTMLPane htmlPane;


    int newTextAction;
    public final static int REPLACE = 0;
    public final static int APPEND = 1;


    String lastTitle;
    String lastText;

    StringBuffer textSB;


    public DruInfoPanel() {
        this("");
    }


    public DruInfoPanel(String text) {
        this(text, 0, 0);
    }


    public DruInfoPanel(String text, int w, int h) {
        super(DruPanel.SCROLLABLE);

        newTextAction = REPLACE;


        htmlPane = new DHTMLPane();
        if (w > 0 && h > 0) {
            htmlPane.setPrefSize(w, h);

        }
        htmlPane.setEditable(false);

        // htmlPane.setEditable(true);
        htmlPane.setDefaultStyleSheet();

        setSingle();
        addDComponent(htmlPane);

        if (text != null) {
            showInfo(text);
        }
    }



    public void setAppendMode() {
        newTextAction = APPEND;
    }


    public void setReplaceMode() {
        newTextAction = REPLACE;
    }


    public void receiveInfo(String txt) {
        E.debugError("plain text info");
        showInfo("", txt);
    }


    public void receiveInfo(String infoTitle, String txt) {
        showInfo(infoTitle, txt);
    }



    public String getHTMLTemplate() {
        if (htmlTemplate == null) {
            htmlTemplate = JUtil.getRelativeResource(new XdocBase(), "InfoHTMLTemplate.txt");
        }
        return htmlTemplate;
    }


    public void showText(String s) {
        showInfo(s);
    }


    public void setBg(Color c) {
        htmlPane.setBackground(c);
        super.setBg(c);
    }


    public void setText(String s) {
        showInfo(s);
    }


    public void showHTMLContent(String s) {
        showInfo(s);
    }


    public void showInfo(String s) {
        showInfo("", s);
    }



    public void showInfo(String itin, String txtin) {
        String infoTitle = itin;
        String txt = txtin;
        if (txt == null) {
            txt = "";
        }

        if (infoTitle == null) {
            infoTitle = "";
        }

        if (lastTitle == null) {
            lastTitle = "";
        }

        if (lastText == null) {
            lastText = "";
        }

        if (infoTitle.equals(lastTitle) && txt.equals(lastText)) {
            // do nothing;

        } else {
            lastTitle = infoTitle;
            lastText = txt;

            if (textSB == null || newTextAction == REPLACE) {
                textSB = new StringBuffer();
            }

            if (infoTitle != null && infoTitle.length() > 0) {
                textSB.append("<h2>");
                textSB.append(infoTitle);
                textSB.append("</h2>\n");
            }

            String[] da = txt.split("-p-");
            for (String apar : da) {
                String stxt = TextTagger.getTagger().htmlizeParagraph(apar);

                // this here because need <br> not <br/> for the text area to render properly;
                stxt = stxt.replaceAll("-b-", "<br>");

                textSB.append("<p>");
                textSB.append(stxt);
                textSB.append("</p>");
            }

            int iep = 0;
            while (textSB.length() > 1000 && (iep = textSB.indexOf("</p>")) > 0) {
                textSB.delete(0, iep + 4);
            }


            String sh = getHTMLTemplate().replaceAll("BODY", textSB.toString());
            htmlPane.showHTML(sh);

            // EFF - this gets called far too often!!!!!!!!!!!!!!!!! - uncomment
            // next to see
            // E.info("info panel showiing " + sh);

            textAdded();
            /*
             * htmlPane.revalidate(); revalidate();
             */
        }
    }


    public void textAdded() {

    }



}
