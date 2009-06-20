package org.catacomb.interlish.report;

import org.catacomb.interlish.structure.InfoLog;
import org.catacomb.interlish.structure.InfoMessage;
import org.catacomb.report.E;


public class UserLog {


    public static InfoLog log;

    public static void setLog(InfoLog lg) {
        log = lg;
    }

    public static void infoMsg(String msgtyp, String msg) {
        if (log == null) {
            E.info("user log needs druid access " +msgtyp + " " +  msg);
        } else {
            log.addInfoMessage(InfoMessage.INFO, msgtyp, msg);

        }
    }

    public static void errorMsg(String msgtyp, String msg) {
        if (log == null) {
            E.info("user log needs druid access " +msgtyp + " " +  msg);

        } else {
            log.addInfoMessage(InfoMessage.ERROR, msgtyp, msg);
        }


    }


}
