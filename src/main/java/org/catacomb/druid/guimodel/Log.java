package org.catacomb.druid.guimodel;

import org.catacomb.interlish.report.UserLog;
import org.catacomb.interlish.structure.InfoLog;
import org.catacomb.interlish.structure.InfoMessage;
import org.catacomb.interlish.structure.LogNotificand;
import org.catacomb.report.E;


import java.util.ArrayList;


// producer of logs (this)
// receiver of log messages
public class Log implements InfoLog {

    String name;

    ArrayList<InfoMessage> messages;
    StringBuffer textBuffer;
    StringBuffer htmlBuffer;

    LogNotificand notificand;

    static Log systemLog;


    public Log(String s) {
        name = s;
        messages = new ArrayList<InfoMessage>();
        clear();
    }



    public void addInfoMessage(InfoMessage im) {
        messages.add(im);
        textBuffer.append(im.getPlainText());
        textBuffer.append("-br-");
        textBuffer.append("\n");

        htmlBuffer.append(im.getHTML());

        if (notificand != null) {
            notificand.itemLogged(this);
        }
    }


    public void addInfoMessage(int lev, String ctx, String msg) {
        addInfoMessage(new LogEntry(lev, ctx, msg));
    }


    public void setLogNotificand(LogNotificand ln) {
        if (notificand != null) {
            E.warning("squashing existing log notificand");
        }
        notificand = ln;
    }

    public void removeLogNotificand(LogNotificand ln) {
        notificand = null;
    }


    public String getPlainText() {
        return textBuffer.toString();
    }


    public String getHTML() {
        return htmlBuffer.toString();
    }


    public void clear() {
        messages.clear();
        textBuffer = new StringBuffer();
        htmlBuffer = new StringBuffer();
    }



    // REFAC - these should go elsewhere...;
    public static void setSystemLog(Log log) {
        systemLog = log;
        UserLog.setLog(log);
        E.setReporter(log);
    }


    public void report(String s) {
        infoMsg("", s);
    }


    public void reportInfo(String s) {
        infoMsg("", s);
    }

    public void reportWarning(String s) {
        warningMsg("", s);
    }

    public void reportError(String s) {
        errorMsg("", s);
    }


    public static void infoMsg(String ctx, String txt) {
        systemLog.addInfoMessage(LogEntry.INFO, ctx, txt);
    }

    public static void warningMsg(String ctx, String txt) {
        systemLog.addInfoMessage(LogEntry.WARNING, ctx, txt);
    }

    public static void errorMsg(String ctx, String txt) {
        systemLog.addInfoMessage(LogEntry.ERROR, ctx, txt);
    }
}
