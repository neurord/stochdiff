package org.catacomb.druid.gui.edit;

import java.awt.Color;
import java.awt.Dimension;

import org.catacomb.druid.swing.DBorderLayout;
import org.catacomb.druid.swing.DHTMLPane;
import org.catacomb.druid.swing.DScrollPane;
import org.catacomb.interlish.structure.Consumer;
import org.catacomb.interlish.structure.InfoLog;
import org.catacomb.interlish.structure.LogDisplay;
import org.catacomb.interlish.structure.LogNotificand;
import org.catacomb.interlish.util.JUtil;
import org.catacomb.report.E;
import org.catacomb.xdoc.XdocBase;



public class DruLogPanel extends DruGCPanel implements LogNotificand, Consumer, LogDisplay, Runnable {

    static final long serialVersionUID = 1001;

    DScrollPane dsp;
    DHTMLPane htmlPane;


    InfoLog infoLog;

    String logHTMLTemplate;


    Thread updateThread;
    boolean pendingEntry = false;


    public DruLogPanel(int h) {
        super();

        setBorderLayout(2, 2);
        dsp = new DScrollPane();
        dsp.setVerticalScrollBarAlways();

        addDComponent(dsp, DBorderLayout.CENTER);

        /*
         DruActionRelay relay = new DruActionRelay(this);
         * drfp = new DruFlowPanel(DruFlowPanel.RIGHT);
         *
         *
         *
         * bclear = new DruButton("clear"); bclear.setActionRelay(relay);
         *
         * bsave = new DruButton("save"); bsave.setActionRelay(relay);
         *
         * drfp.addPanel(bclear); drfp.addPanel(bsave);
         *
         * addPanel(drfp, DBorderLayout.SOUTH);
         */

        htmlPane = new DHTMLPane();

        htmlPane.setRelStyleSheet(new XdocBase(), "LogCSS.txt");


        dsp.setViewportView(htmlPane);

        setTooltipTarget(htmlPane);
    }



    public String getLogHTMLTemplate() {
        if (logHTMLTemplate == null) {
            logHTMLTemplate = JUtil.getRelativeResource(new XdocBase(), "LogHTMLTemplate.txt");
        }
        return logHTMLTemplate;
    }



    public void addInfoLog(InfoLog ilog) {
        if (infoLog != null) {
            E.warning("squashing existing log");
            infoLog.removeLogNotificand(this);
        }
        infoLog = ilog;

        infoLog.setLogNotificand(this);
    }



    public void setPreferredSize(int w, int h) {
        dsp.setPreferredSize(new Dimension(w, h));
        setPreferredSize(new Dimension(w, h));
    }


    public void setBg(Color c) {

        dsp.setBg(c);
        htmlPane.setBg(c);

        /*
         * drfp.setBg(c); bsave.setBg(c); bclear.setBg(c);
         */

        super.setBg(c);
    }


    public void setNoHorizontalScroll() {
        dsp.setHorizontalScrollbarNever();
    }



    public void clear() {
        if (infoLog != null) {
            infoLog.clear();
            showHTML("");
        }
    }


    public void save() {
        E.missing();
    }



    public void itemLogged(InfoLog ilog) {
        if (infoLog != ilog) {
            E.error("log mixup?");
        }
        pendingEntry = true;

        if (updateThread == null) {
            updateThread = new Thread(this);
            updateThread.setPriority(Thread.MIN_PRIORITY);
            updateThread.start();
        }
    }


    public void run() {
        try {
            while (true) {
                Thread.sleep(2000);
                if (pendingEntry) {
                    pendingEntry = false;
                    String sh = infoLog.getHTML();
                    showHTML(sh);
                }
            }
        } catch (Exception ex) {
            E.info("thread died?");
        }
    }






    public void showHTML(String sin) {
        String s = sin;
        if (s == null) {
            s = "";
        }

        String txt = getLogHTMLTemplate();
        txt = txt.replaceAll("BODY", s);
        htmlPane.showHTML(txt);
    }



    public void showText(String sin) {
        String s = sin;
        if (s == null) {
            s = "";
        }
        s = s.replaceAll("-p-", "</p>\n<p>");
        s = s.replaceAll("-br-", "<br>\n");

        String sh = "<p>" + s + "</p>";

        htmlPane.showHTML(sh);
        /*
         * htmlPane.revalidate(); revalidate();
         */
    }


}
