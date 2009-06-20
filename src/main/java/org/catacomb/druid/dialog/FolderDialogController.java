package org.catacomb.druid.dialog;

import org.catacomb.druid.gui.base.DruInfoPanel;
import org.catacomb.druid.util.FileChooser;
import org.catacomb.interlish.annotation.Editable;
import org.catacomb.interlish.annotation.IOPoint;
import org.catacomb.interlish.content.StringValue;

import java.io.File;

public class FolderDialogController extends DialogController {


    @Editable(xid="nameTF")
    public StringValue nameSV;


    @IOPoint(xid="info")
    public DruInfoPanel infoPanel;

    File returnValue;

    File initFile;


    public FolderDialogController() {
        super();
        nameSV = new StringValue("");
    }



    public void OK() {
        returnValue = new File(nameSV.getString());
        hideDialog();
    }

    public void cancel() {
        returnValue = null;
        hideDialog();
    }


    public void chooseFolder() {
        checkInit();
        File fc = FileChooser.getChooser().getFolder(initFile);
        if (fc != null) {
            nameSV.reportableSetString(fc.getAbsolutePath(), this);
        }
    }


    public File getFolder(int[] xy, String msg, File fdef) {
        checkInit();
        initFile = fdef;
        returnValue = null;
        nameSV.reportableSetString(fdef.getAbsolutePath(), this);
        infoPanel.showInfo(msg);
        infoPanel.revalidate();
        showModalAt(xy[0], xy[1]);

        return returnValue;
    }

}
