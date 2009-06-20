
package org.catacomb.druid.blocks;

import org.catacomb.druid.gui.base.DruGridPanel;
import org.catacomb.druid.gui.base.DruPanel;



public class GridPanel extends MultiPanel {


    public int nrow;
    public int ncolumn;





    public DruPanel instantiatePanel() {
        int nel = getPanelCount();


        if (nrow == 0 && ncolumn == 0) {
            nrow = 1;
        }

        if (nrow == 0 && ncolumn > 0) {
            nrow = (nel-1)/ncolumn + 1;

        } else if (ncolumn == 0) {
            ncolumn = (nel - 1) / nrow + 1;
        }

        if (nrow <= 0) {
            nrow = 1;
        }

        if (ncolumn <= 0) {
            ncolumn = 1;
        }

        return new DruGridPanel(nrow, ncolumn, xspace, yspace);
    }

}
