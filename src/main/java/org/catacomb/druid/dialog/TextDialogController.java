package org.catacomb.druid.dialog;

import org.catacomb.druid.gui.base.DruScrollingHTMLPanel;
import org.catacomb.interlish.annotation.IOPoint;
import org.catacomb.interlish.content.StringValue;


public class TextDialogController extends DialogController {

//  @IOPoint(xid="text")
// public DruTextArea textArea;

    @IOPoint(xid="text")
    public DruScrollingHTMLPanel htmlPanel;



    public void close() {

        hideDialog();
    }



    public void showNonModal(int[] xy, StringValue txtCtnr) {
        checkInit();
        /*
         textArea.setStringValue(txtCtnr);
         textArea.setEditable(false);
         */

        htmlPanel.setStringValue(txtCtnr);

        showNonModalAt(xy[0], xy[1]);

    }



    public void show(int[] xy, StringValue txtCtnr) {
        checkInit();

        // textArea.setStringValue(txtCtnr);
        htmlPanel.setStringValue(txtCtnr);

        showModalAt(xy[0], xy[1]);

    }

}
