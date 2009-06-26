package org.catacomb.druid.build;


import org.catacomb.druid.dialog.Dialoguer;
import org.catacomb.interlish.structure.InfoExporter;
import org.catacomb.interlish.structure.InfoReceiver;
import org.catacomb.report.E;



public class InfoAggregator implements InfoReceiver, InfoExporter {


    InfoReceiver infoReceiver;

    String bufTitle;
    String bufText;

    int nbuf;

    public InfoAggregator() {
        nbuf = 0;
    }



    public void setInfoReceiver(InfoReceiver ir) {
        infoReceiver = ir;

        if (bufTitle != null) {
            receiveInfo(bufTitle, bufText);

        } else if (bufText != null) {
            receiveInfo("Introduction", bufText);
        }
    }


    public void receiveInfo(String s) {
        receiveInfo("", s);

    }

    public void receiveInfo(String title, String text) {
        if (infoReceiver != null) {
            infoReceiver.receiveInfo(title, text);
        } else {
            bufTitle = title;
            bufText = text;

            if (nbuf == 0) {
                // E.oneLineWarning("gui info is being dropped since there is no display for it");
                nbuf = 1;
            }
            //          checkNbuf();
//          Dialoguer.message(null, title, text);
        }
    }

    private void checkNbuf() {
        nbuf += 1;
        if (nbuf > 4) {
            E.oneLineWarning("multiple info sent to agregator without outlet");
        }
    }


}
