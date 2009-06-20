package org.catacomb.druid.dialog;

import org.catacomb.druid.gui.base.DruInfoPanel;
import org.catacomb.druid.gui.edit.DruButton;
import org.catacomb.interlish.annotation.IOPoint;
import org.catacomb.report.E;


public class QuestionDialogController extends DialogController {

    @IOPoint(xid="message")
    public DruInfoPanel infoPanel;


    @IOPoint(xid="but1")
    public DruButton but1;

    @IOPoint(xid="but2")
    public DruButton but2;

    @IOPoint(xid="but3")
    public DruButton but3;

    int retval;



    public void button1() {
        retval = 0;
        hideDialog();
    }


    public void button2() {
        retval = 1;
        hideDialog();
    }


    public void button3() {
        retval = 2;
        hideDialog();
    }




    public int getResponse(String msg, String[] aa) {
        checkInit();
        retval = -1;

        if (aa.length == 3) {
            but1.setLabelText(aa[0]);
            but2.setLabelText(aa[1]);
            but3.setLabelText(aa[2]);

        } else {
            E.error("only handle 3 responses as yet!!!");
        }
        infoPanel.setReplaceMode();
        infoPanel.showInfo(msg);
        infoPanel.revalidate();
        showModalAt(400, 300);

        return retval;
    }


}
