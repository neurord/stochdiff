package org.catacomb.druid.swing.dnd;

import org.catacomb.report.E;

import javax.swing.TransferHandler;
import javax.swing.JComponent;
import java.awt.datatransfer.DataFlavor;


public class InternalTransferHandler extends TransferHandler {
    private static final long serialVersionUID = 1L;

    public InternalTransferHandler() {
        super();
    }


    public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
        E.info("DTransferHandler asked about import of " + transferFlavors);
        return true;
    }

}
