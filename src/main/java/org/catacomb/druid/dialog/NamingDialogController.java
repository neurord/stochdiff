package org.catacomb.druid.dialog;

import org.catacomb.druid.gui.base.DruInfoPanel;
import org.catacomb.interlish.annotation.Editable;
import org.catacomb.interlish.annotation.IOPoint;
import org.catacomb.interlish.content.StringValue;



public class NamingDialogController extends DialogController {

    @Editable(xid="nameTF")
    public StringValue nameSV;

    @IOPoint(xid="info")
    public DruInfoPanel infoPanel;

    String returnValue;


    public NamingDialogController() {
        super();
        nameSV = new StringValue("");
    }



    public void OK() {
        returnValue = nameSV.getString();
        hideDialog();
    }

    public void cancel() {
        returnValue = null;
        hideDialog();
    }


    public String getNewName(int[] xy, String msg, String initValue) {
        checkInit();
        returnValue = null;
        nameSV.reportableSetString(initValue, this);
        infoPanel.showInfo(msg);
        infoPanel.revalidate();
        showModalAt(xy[0], xy[1]);

        return returnValue;
    }

}
