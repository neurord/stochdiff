package org.catacomb.druid.guimodel;

import org.catacomb.interlish.structure.InfoMessage;
import org.catacomb.interlish.util.JUtil;
import org.catacomb.util.Timestamp;



public class LogEntry implements InfoMessage {

    String timestamp;
    int severity;
    String context;
    String text;

    String logEntryTemplate;
    String htmlText;



    public LogEntry(String s) {
        this(INFO, "default", s);
    }


    public LogEntry(int level, String msg) {
        this(level, "default", msg);
    }


    public LogEntry(int level, String ctx, String msg) {
        severity = level;
        context = ctx;
        text = msg;

        timestamp = Timestamp.withinSessionTimestamp();

    }



    public String getPlainText() {
        return (timestamp + " " + textLevels[severity] + " " + context + " " + text);
    }



    public String getHTML() {
        if (htmlText == null) {
            if (logEntryTemplate == null) {
                logEntryTemplate = JUtil.getRelativeResource(this, "LogEntryTemplate.txt");
            }
            String s = logEntryTemplate.replaceAll("LEVEL", "" + textLevels[severity]);
            s = s.replaceAll("TIMESTAMP", timestamp);
            s = s.replaceAll("CONTEXT", context);
            s = s.replaceAll("TEXT", text);

            htmlText = s;
        }
        return htmlText;
    }


    public int getLevel() {
        return severity;
    }


    public String getContext() {
        return context;
    }


    public boolean sameAs(InfoMessage im) {
        boolean ret = false;

        if (im instanceof LogEntry) {
            LogEntry le = (LogEntry)im;
            if (le.getPlainText().equals(getPlainText())) {
                ret = true;
            }
        }
        return ret;
    }

}
