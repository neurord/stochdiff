package org.catacomb.interlish.structure;

import org.catacomb.report.Reporter;



public interface InfoLog extends Receiver, Producer, Reporter {

    void addInfoMessage(InfoMessage im);

    void addInfoMessage(int level, String ctxt, String msg);

    String getPlainText();

    String getHTML();

    void setLogNotificand(LogNotificand ln);

    void removeLogNotificand(LogNotificand ln);

    void clear();

}
